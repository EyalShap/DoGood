package com.dogood.dogoodbackend.emailverification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username}}")
    private String mailFromAddress;

    @Async
    public void sendVerificationCodeEmail(String toEmail, String username, String code) {
        String subject = "DoGood Account - Your Verification Code";
        String text = String.format(
                "Hello %s,\n\nYour verification code for DoGood is: %s\n\nPlease enter this code in the application within 5 minutes to complete your registration.\n\nIf you did not request this, please ignore this email.\n\nThank you,\nThe DoGood Team",
                username,
                code
        );
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // TEMPORARY DEBUG LINE START
            System.out.println("DEBUG: EmailService - mailFromAddress resolved to: [" + mailFromAddress + "]");
            // TEMPORARY DEBUG LINE END

            if (mailFromAddress == null || mailFromAddress.isEmpty()) {
                System.err.println("ERROR: Email 'from' address is not configured in application.properties (spring.mail.from or spring.mail.username). Cannot send email.");
                throw new RuntimeException("Email sender address not configured.");
            }
            message.setFrom(mailFromAddress); // This is where the error originates
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Verification code email sent successfully to " + toEmail);
        } catch (MailException e) {
            System.err.println("FATAL: Error sending verification code email to " + toEmail + ": " + e.getMessage());
            // Log the root cause if it's an AddressException
            if (e.getCause() instanceof jakarta.mail.internet.AddressException) {
                System.err.println("ROOT CAUSE (AddressException): " + e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) { // Catch any other unexpected exception
            System.err.println("UNEXPECTED FATAL: Error sending verification code email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for unexpected errors
            throw new RuntimeException("Failed to send verification email due to unexpected error", e);
        }
    }
}