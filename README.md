# Email Verification Lambda Function

This repository contains a Lambda function written in Java that processes SNS messages and sends verification emails to users. The function is designed to work as part of an email verification system.

## Features

- **SNS Subscription**: Listens to messages from an AWS SNS topic.
- **Message Parsing**: Extracts the recipient email and a unique token from the message payload.
- **Email Sending**: Constructs a verification link and sends it to the recipient using the Mailgun API.

## Prerequisites

Before running the function, ensure the following environment variables are set:

| Environment Variable | Description                                          |
|-----------------------|------------------------------------------------------|
| `MAILGUN_API_KEY`     | API key for authenticating with the Mailgun API.     |
| `MAILGUN_DOMAIN`      | Your Mailgun domain (e.g., `dev/demo.name.me`).      |
| `FROM_EMAIL`          | The sender email address used in outgoing emails.    |
| `BASE_URL`            | The base URL for constructing the verification link. |

## Code Structure

### Main Components
- **`EmailHandler`**:
    - Implements the `RequestHandler` interface.
    - Processes incoming SNS messages and sends verification emails.

- **`MailService`**:
    - Handles the communication with the Mailgun API for sending emails.

### How It Works
1. The Lambda function is triggered by an SNS message.
2. The message is parsed to extract the `email` and `token`.
3. A verification link is constructed using the `BASE_URL` and `token`.
4. The email is sent to the recipient using the Mailgun API.