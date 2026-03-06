package ca.yorku.my.StudyBuddy.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.AuthService;

import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
/**
 * This class handles authentication-related HTTP endpoints that is used by the frontend.
 *
 * This controller delegates business logic to AuthService and converts
 * service outcomes into user-facing HTTP responses.
 */
public class AuthController {

    @Autowired
    private AuthRepository authService;

    /**
     * Registers a new student account and triggers an email verification flow.
     */
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
            String msg = e.getMessage();
            if (msg != null && msg.contains("EMAIL_EXISTS")) {
                return ResponseEntity.status(409).body("Registration Failed: " + msg);
            }
            return ResponseEntity.badRequest().body("Registration Failed: " + msg);
        }
    }

    /**
     * This method generates a password reset link for an existing account email.
     */
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

/**
 * Logs a user in by validating account state and returning a session token.
 */
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