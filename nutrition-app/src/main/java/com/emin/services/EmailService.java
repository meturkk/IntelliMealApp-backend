package com.emin.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setSubject("IntelliMeal Email Doğrulama Kodu");
            message.setText("Doğrulama kodunuz: " + code + "\n\nEğer bu isteği siz yapmadıysanız, bu e-postayı dikkate almayınız.");
            mailSender.send(message);
        } catch (Exception e) {
            // Log or handle as needed; for now just print stack trace
            e.printStackTrace();
        }
    }
}
