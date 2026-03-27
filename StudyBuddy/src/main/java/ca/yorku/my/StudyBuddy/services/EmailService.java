package ca.yorku.my.StudyBuddy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
/**
 * This class sends plain-text transactional emails through Spring Mail.
 */
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This method sends a plain text email to any YorkU address
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("therealyourstudybuddy@gmail.com"); // Matches application.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}