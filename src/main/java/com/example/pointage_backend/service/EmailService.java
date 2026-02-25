package com.example.pointage_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${frontend.reset-password-url}")
    private String frontendResetPasswordUrl;
    
    public boolean sendPasswordResetEmail(String email, String resetToken, String username) {
        try {
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Cannot send email: recipient email is null or empty");
                return false;
            }
            
            String resetLink = frontendResetPasswordUrl + "?token=" + resetToken;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(email);
            helper.setSubject("Password Reset Request - Pointage System");
            
            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<p>Hello <strong>" + username + "</strong>,</p>" +
                    "<p>You requested a password reset. Click the link below to reset your password:</p>" +
                    "<p><a href='" + resetLink + "' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>Reset Password</a></p>" +
                    "<p>Or copy this token: <code style='background-color: #f4f4f4; padding: 5px 10px; border-radius: 3px;'>" + resetToken + "</code></p>" +
                    "<p><small>This token will expire in 30 minutes.</small></p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "<p>Best regards,<br/><strong>Pointage Team</strong></p>" +
                    "</body></html>";
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: " + email);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send password reset email to " + email + ": " + e.getMessage(), e);
            return false;
        }
    }

    public boolean sendNewAccountEmail(String email, String username, String password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Cannot send email: recipient email is null or empty");
                return false;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Welcome to Pointage System - New Account Created");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<p>Hello <strong>" + username + "</strong>,</p>" +
                    "<p>An account has been created for you in the Pointage System.</p>" +
                    "<p>Here are your login credentials:</p>" +
                    "<ul>" +
                    "<li><strong>Username:</strong> " + username + "</li>" +
                    "<li><strong>Password:</strong> " + password + "</li>" +
                    "</ul>" +
                    "<p>Please log in and change your password as soon as possible.</p>" +
                    "<p>Best regards,<br/><strong>Pointage Team</strong></p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("New account email sent successfully to: " + email);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send new account email to " + email + ": " + e.getMessage(), e);
            return false;
        }
    }
}
