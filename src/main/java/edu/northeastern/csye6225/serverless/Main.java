package edu.northeastern.csye6225.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, String> {

    private final EmailHandler emailHandler;

    public Main() {
        // Instantiate the EmailHandler
        this.emailHandler = new EmailHandler();
    }

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        System.out.println("Main.handleRequest invoked with event: " + event);

        // Delegate to EmailHandler
        try {
            return emailHandler.handleRequest(event, context);
        } catch (Exception e) {
            System.err.println("Error in EmailHandler: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {

        // Simulate an SNS event for local testing
        Map<String, Object> event = Map.of(
                "Records", List.of(
                        Map.of(
                                "EventSource", "aws:sns",
                                "Sns", Map.of(
                                        "Message", "{\"email\":\"test3@example.com\",\"token\":\"abcd1234\"}",
                                        "Timestamp", "2024-11-16T00:42:24.226Z"
                                )
                        )
                )
        );

        Main lambdaHandler = new Main();
        String result = lambdaHandler.handleRequest(event, null);
        System.out.println("Result: " + result);

        System.out.println("Run as a standalone Java application for debugging or testing!");
    }
}