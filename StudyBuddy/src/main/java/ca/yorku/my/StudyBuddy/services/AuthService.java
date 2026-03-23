package ca.yorku.my.StudyBuddy.services;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Student;

@Service
@Profile("firestore")
public class AuthService implements AuthRepository {

    @Autowired
    private EmailService emailService;

    /**
     * Registers a new student, saves their profile to NoSQL, 
     * and sends a verification email.
     * (e.x)
     * {
	    "first name" : "Vaughn",
	    "last name" : "Chan",
	    "email": "vc8@my.yorku.ca",
	    "major": "Software Engineering",
	    "year": "2022",
	    "password": "test123"
		}
     */
    public String registerUser(String email, String password, String firstName, String lastName, String major, String year) throws Exception {

        // 1. UNIVERSITY GUARD: Restrict registration to YorkU domains
        if (!email.endsWith("@yorku.ca") && !email.endsWith("@my.yorku.ca")) {
            throw new IllegalArgumentException("Access Denied: Only YorkU emails are allowed.");
        }

        // 2. AUTHENTICATION: Create the account in Firebase Auth
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
            .setEmail(email)
            .setPassword(password)
            .setDisplayName("DELETE ME");

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        
        // 2.5 Create the student
        String uid = userRecord.getUid();

        // Immediately create their linked Firestore document
	     Student newStudent = new Student();
	     newStudent.setUserId(uid);
	     newStudent.setEmail(email);
	     newStudent.setFirstName(firstName);
	     newStudent.setLastName(lastName);
	     newStudent.setProgram(major);
	     newStudent.setYear(year);
	     
	     LocalDate ld = LocalDate.now();
	     newStudent.setJoinedDate(ld.toString());
	     
	     Firestore db = FirestoreClient.getFirestore();
	     db.collection("students").document(uid).set(newStudent);

        // 3. EMAIL OWNERSHIP: Generate and send the verification link
        String vLink = FirebaseAuth.getInstance().generateEmailVerificationLink(email);
        String emailBody = "Welcome to StudyBuddy! Please verify your YorkU account to start connecting with peers:\n\n" + vLink;
        emailService.sendEmail(email, "Verify Your StudyBuddy Account", emailBody);

        // 4. DATABASE: Save the student profile as a NoSQL document
        String generatedId = email.substring(0, email.indexOf("@"));

        if (generatedId == null || generatedId.isEmpty()) {
            throw new IllegalArgumentException("Invalid email format for ID generation.");
}

		//student.setUserId(generatedId);
		
		//Firestore db = FirestoreClient.getFirestore();
		// The error below should now vanish because you've proven generatedId isn't null
		//db.collection("users").document(generatedId).set(student);
		
        	return userRecord.getUid();
    	}

    /**
     * Validates credentials and checks if the student has verified their email.
     * Issues a Session Token if successful, as well as password.
     */
    @Value("${firebase.web.api.key}")
    private String webApiKey;
    public String loginUser(String email, String password) throws Exception {
        // 1. Safely format the JSON payload
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("email", email);
        bodyData.put("password", password);
        bodyData.put("returnSecureToken", true);
        String payload = mapper.writeValueAsString(bodyData);

        // 2. Ping the Firebase REST API to verify the password
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + webApiKey;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("Login Failed: Incorrect email or password.");
        }

        // 1. Extract the secure ID Token from Google's response
        JsonNode rootNode = mapper.readTree(response.body());
        String idToken = rootNode.path("idToken").asText();

        // 2. Fetch user to check email verification status
        UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Login Denied: Please verify your YorkU email first.");
        }

        // 3. Update login streak for the user
        String uid = user.getUid();
        String today = LocalDate.now().toString(); // ISO format: yyyy-MM-dd
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("students").document(uid);
            DocumentSnapshot doc = docRef.get().get();
            if (doc.exists()) {
                String lastLogin = doc.getString("lastLoginDate");
                Long storedStreak = doc.getLong("loginStreak");
                int currentStreak = storedStreak != null ? storedStreak.intValue() : 0;
                int newStreak;
                if (lastLogin == null) {
                    newStreak = 1;
                } else if (lastLogin.equals(today)) {
                    // Already logged in today — keep existing streak unchanged
                    newStreak = currentStreak;
                } else {
                    LocalDate lastLoginDate = LocalDate.parse(lastLogin);
                    long daysBetween = ChronoUnit.DAYS.between(lastLoginDate, LocalDate.now());
                    if (daysBetween == 1) {
                        newStreak = currentStreak + 1; // Consecutive day
                    } else {
                        newStreak = 1; // Streak broken — reset
                    }
                }
                if (!today.equals(lastLogin)) {
                    docRef.update("loginStreak", newStreak, "lastLoginDate", today).get();
                }
            }
        } catch (Exception e) {
            // Streak update failure should not block login
        }

        // 4. Return the real ID Token!
        return idToken;
    }
    
    public String verifyFrontendToken(String authHeader) throws Exception {
        // 1. Check if the header exists and is formatted properly
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header.");
        }

        // 2. Extract just the token string (remove "Bearer ")
        String token = authHeader.substring(7);

        // 3. Ask Firebase to verify it
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

        // 4. Return the secure UID!
        return decodedToken.getUid();
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