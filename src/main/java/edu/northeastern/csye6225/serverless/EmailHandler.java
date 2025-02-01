package edu.northeastern.csye6225.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.regions.Region;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmailHandler implements RequestHandler<Map<String, Object>, String> {

    private final MailService mailService;
    //private final DatabaseHandler databaseHandler;
    private final String baseUrl;

    public EmailHandler(){

        String secretName = System.getenv("EMAIL_SECRET_NAME");
        String awsRegion = System.getenv("AWS_REGION_NAME");

        //Region region = Region.US_EAST_1;
        Region region = Region.of(awsRegion);

        // Load Mailgun credentials from Secrets Manager
        SecretsManagerClient secretsManagerClient = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest secretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        GetSecretValueResponse secretValueResponse = secretsManagerClient.getSecretValue(secretValueRequest);

        String secretString = secretValueResponse.secretString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> secrets;
        try {
            secrets = objectMapper.readValue(secretString, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing secrets JSON", e);
        }

        String mailgunApiKey = secrets.get("api_key");
        String mailgunDomain = secrets.get("domain");
        String mailgunFromEmail = secrets.get("from_email");
        this.baseUrl = secrets.get("base_url");

        System.out.println("Loaded Mailgun credentials and base URL from Secrets Manager.");

        this.mailService = new MailService(mailgunApiKey, mailgunDomain, mailgunFromEmail);

    }

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        try {
            // Extract the "Records" field
            List<Map<String, Object>> records = (List<Map<String, Object>>) event.get("Records");

            for (Map<String, Object> record : records) {
                // Extract the SNS message
                Map<String, Object> sns = (Map<String, Object>) record.get("Sns");
                String messageJson = (String) sns.get("Message");

                // Deserialize the message JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> message = objectMapper.readValue(messageJson, Map.class);

                String recipientEmail = message.get("email");
                String token = message.get("token");

                // Validate input
                if (recipientEmail == null || recipientEmail.isEmpty()) {
                    throw new IllegalArgumentException("Recipient email is required");
                }
                if (token == null || token.isEmpty()) {
                    throw new IllegalArgumentException("Token is required");
                }

                System.out.printf("Email: %s, Token: %s%n", recipientEmail, token);

                // Construct verification URL
                String verificationUrl = String.format(
                        "https://" + baseUrl + "/verify?user=%s&token=%s",
                        recipientEmail, token
                );

                // Prepare email body
                String emailBody = String.format(
                        "Dear User,\n\n" +
                                "Please verify your identity by clicking the link below:\n\n" +
                                "%s\n\n" +
                                "If you did not request this verification, please ignore this email.\n\n" +
                                "Best regards,\nYour Team",
                        verificationUrl
                );

                // Send email
                mailService.sendEmail(recipientEmail, emailBody);

                // Log email in the database
                EmailLog emailLog = new EmailLog();
                emailLog.setId(UUID.randomUUID());
                emailLog.setRecipientEmail(recipientEmail);
                emailLog.setEmailBody(emailBody);
                emailLog.setSentAt(Timestamp.from(Instant.now()));
//                try {
//                    databaseHandler.saveEmailLog(emailLog);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            }

            return "Verification email sent successfully!";
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}