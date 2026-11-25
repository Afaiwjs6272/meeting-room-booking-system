package ru.company.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String text) {
        try {
            log.info("📧 Attempting to send email");
            log.info("   From: {}", fromEmail);
            log.info("   To: {}", to);
            log.info("   Subject: {}", subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}