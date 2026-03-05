package ca.yorku.my.StudyBuddy.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.AuthService;

import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        try {
            String firebaseUid = authService.registerUser(
            		body.get("email"), 
            		body.get("password"),
            		body.get("firstName"),
            		body.get("lastName"),
            		body.get("major"),
            		body.get("year"));
            return ResponseEntity.ok("Verification email sent! UID: " + firebaseUid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration Failed: " + e.getMessage());
        }
    }

    // New: Password Reset Feature
    @PostMapping("/reset-password")
public ResponseEntity<String> resetPassword(@RequestParam(required = false) String email) {
    // 1. Check if the frontend actually sent an email
    if (email == null || email.isEmpty()) {
        return ResponseEntity.badRequest().body("Error: Please enter your YorkU email first.");
    }

    try {
        authService.generateResetLink(email);
        return ResponseEntity.ok("Reset link generated for " + email);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("An unexpected error occurred.");
    }
}

@PostMapping("/login")
public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
    try {
        String sessionToken = authService.loginUser(body.get("email"), body.get("password"));
        return ResponseEntity.ok(sessionToken);
    } catch (IllegalStateException e) {
        return ResponseEntity.status(403).body(e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Login Failed: User not found.");
    }
}
}