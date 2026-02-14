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

    public String registerUser(Student student, String password) throws Exception {
        String email = student.getEmail().toLowerCase();

        // UNIVERSITY GUARD: Restrict to YorkU domains
        if (!email.endsWith("@yorku.ca") && !email.endsWith("@my.yorku.ca")) {
            throw new IllegalArgumentException("Access Denied: YorkU emails only.");
        }

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
            .setEmail(email)
            .setPassword(password)
            .setDisplayName(student.getFullName());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        // Send verification link
        String vLink = FirebaseAuth.getInstance().generateEmailVerificationLink(email);
        emailService.sendEmail(email, "Verify Your StudyBuddy Account", "Click here: " + vLink);

        // Null safety check to satisfy @Nonnull warning
        String generatedId = email.split("@")[0];
        if (generatedId == null || generatedId.isEmpty()) {
            throw new IllegalArgumentException("Invalid ID generation.");
        }

        student.setUserId(generatedId);
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(generatedId).set(student);

        return userRecord.getUid();
    }

    public String loginUser(String email) throws Exception {
        UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email.toLowerCase());

        // THE LOCK: Only allow entry if verified in Firebase
        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Access Denied: Please verify your YorkU email first.");
        }

        return FirebaseAuth.getInstance().createCustomToken(user.getUid());
    }

    public void generateResetLink(String email) throws Exception {
        FirebaseAuth.getInstance().getUserByEmail(email);
        String resetLink = FirebaseAuth.getInstance().generatePasswordResetLink(email);
        emailService.sendEmail(email, "StudyBuddy Password Reset", "Link: " + resetLink);
    }
}