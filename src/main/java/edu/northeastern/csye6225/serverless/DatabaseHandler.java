package edu.northeastern.csye6225.serverless;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseHandler {

    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;

    public DatabaseHandler(String dbUrl, String dbUsername, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    public void saveEmailLog(EmailLog emailLog) {
        String insertSQL = "INSERT INTO email_logs (id, recipient_email, email_body, sent_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setObject(1, emailLog.getId());
            preparedStatement.setString(2, emailLog.getRecipientEmail());
            preparedStatement.setString(3, emailLog.getEmailBody());
            preparedStatement.setTimestamp(4, emailLog.getSentAt());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save email log: " + e.getMessage(), e);
        }
    }
}