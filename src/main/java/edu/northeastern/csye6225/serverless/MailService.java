package edu.northeastern.csye6225.serverless;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;

public class MailService {

    private final String mailgunApiKey;
    private final String mailgunDomain;
    private final String mailgunFromEmail;

    public MailService(String mailgunApiKey, String mailgunDomain, String mailgunFromEmail) {
        this.mailgunApiKey = mailgunApiKey;
        this.mailgunDomain = mailgunDomain;
        this.mailgunFromEmail = mailgunFromEmail;
    }

    public void sendEmail(String recipientEmail, String emailBody) {
        System.out.println("Preparing to send email...");
        System.out.printf("Recipient: %s, From: %s, Domain: %s%n", recipientEmail, mailgunFromEmail, mailgunDomain);

        try {
            // Initialize Mailgun client
            MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailgunApiKey).createApi(MailgunMessagesApi.class);

            // Prepare the message
            Message message = Message.builder()
                    .from(mailgunFromEmail)
                    .to(recipientEmail)
                    .subject("Verify Your Identity")
                    .text(emailBody)
                    .build();

            // Send the email
            MessageResponse response = mailgunMessagesApi.sendMessage(mailgunDomain, message);

            if (response != null && response.getId() != null) {
                System.out.printf("Email sent successfully. Response ID: %s%n", response.getId());
            } else {
                System.out.println("Failed to send email. No response ID returned.");
            }
        } catch (Exception e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}