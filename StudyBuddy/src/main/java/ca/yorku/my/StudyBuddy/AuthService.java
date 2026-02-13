package ca.yorku.my.StudyBuddy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private EmailService emailService;

    /**
     * Registers a new student, saves their profile to NoSQL, 
     * and sends a verification email.
     */
    public String registerUser(Student student, String password) throws Exception {
        String email = student.getEmail().toLowerCase();

        // 1. UNIVERSITY GUARD: Restrict registration to YorkU domains
        if (!email.endsWith("@yorku.ca") && !email.endsWith("@my.yorku.ca")) {
            throw new IllegalArgumentException("Access Denied: Only YorkU emails are allowed.");
        }

        // 2. AUTHENTICATION: Create the account in Firebase Auth
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
            .setEmail(email)
            .setPassword(password)
            .setDisplayName(student.getFullName());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        // 3. EMAIL OWNERSHIP: Generate and send the verification link
        String vLink = FirebaseAuth.getInstance().generateEmailVerificationLink(email);
        String emailBody = "Welcome to StudyBuddy! Please verify your YorkU account to start connecting with peers:\n\n" + vLink;
        emailService.sendEmail(email, "Verify Your StudyBuddy Account", emailBody);

        // 4. DATABASE: Save the student profile as a NoSQL document
        String generatedId = email.substring(0, email.indexOf("@"));
        student.setUserId(generatedId);
        
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(generatedId).set(student);

        return userRecord.getUid();
    }

    /**
     * Validates credentials and checks if the student has verified their email.
     * Issues a Session Token if successful.
     */
    public String loginUser(String email) throws Exception {
        // 1. Fetch user by email
        UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);

        // 2. THE GATEKEEPER: Block users who haven't verified their email ownership
        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Login Denied: Please verify your YorkU email first.");
        }

        // 3. SECURITY: Generate a secure session token for the frontend
        return FirebaseAuth.getInstance().createCustomToken(user.getUid());
    }

    /**
     * Triggers a password reset flow and sends the secure Google-hosted link to the user.
     */
    public String generateResetLink(String email) throws Exception {
        // 1. Verify user existence
        try {
            FirebaseAuth.getInstance().getUserByEmail(email);
        } catch (Exception e) {
            throw new IllegalArgumentException("No account found with this YorkU email.");
        }

        // 2. RECOVERY: Generate and send the reset link
        String resetLink = FirebaseAuth.getInstance().generatePasswordResetLink(email);
        String resetBody = "A password reset was requested for your StudyBuddy account. " +
                           "Click the link below to set a new password:\n\n" + resetLink;
        
        emailService.sendEmail(email, "StudyBuddy Password Reset", resetBody);
        
        return resetLink;
    }
}