package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.EmailService;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String subject = body.get("subject");
        String message = body.get("body");
        // Basic validation: only allow YorkU emails
        if (to == null || !to.endsWith("@my.yorku.ca")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid recipient email.");
        }
        try {
            emailService.sendEmail(to, subject, message);
            return ResponseEntity.ok("Email sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email.");
        }
    }
}
