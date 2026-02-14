package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Student student, @RequestParam String password) {
        try {
            // Using the variable to avoid the 'unused' warning
            String firebaseUid = authService.registerUser(student, password);
            return ResponseEntity.ok("Verification email sent! ID: " + firebaseUid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration Failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email) {
        try {
            // This triggers the strict .isEmailVerified() check in AuthService
            String token = authService.loginUser(email);
            return ResponseEntity.ok(token);
        } catch (IllegalStateException e) {
            // Sends 403 if the user hasn't clicked the link in their YorkU inbox
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Failed: Account not found.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam(required = false) String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Please enter your email first.");
        }
        try {
            authService.generateResetLink(email);
            return ResponseEntity.ok("Reset link generated for " + email);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}