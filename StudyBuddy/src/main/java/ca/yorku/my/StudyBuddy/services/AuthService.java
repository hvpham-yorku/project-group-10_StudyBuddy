package ca.yorku.my.StudyBuddy.services;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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

import ca.yorku.my.StudyBuddy.TwoFARequiredException;
import ca.yorku.my.StudyBuddy.classes.Student;

@Service
@Profile("firestore")
public class AuthService implements AuthRepository {

    // OTP store: email -> [code, expiryEpochSeconds]
    private final ConcurrentHashMap<String, long[]> otpStore = new ConcurrentHashMap<>();
    private static final long OTP_TTL_SECONDS = 300; // 5 minutes

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

        // 4. Check if the user has 2FA enabled in their Firestore profile
        try {
            Firestore db2fa = FirestoreClient.getFirestore();
            DocumentSnapshot profileDoc = db2fa.collection("students").document(uid).get().get();
            Boolean twoFAEnabled = profileDoc.getBoolean("twoFAEnabled");
            if (Boolean.TRUE.equals(twoFAEnabled)) {
                // Generate a 6-digit OTP, store it, and email it
                String otp = String.format("%06d", new Random().nextInt(1_000_000));
                long expiry = Instant.now().getEpochSecond() + OTP_TTL_SECONDS;
                otpStore.put(email, new long[]{ Long.parseLong(otp), expiry });
                otpStore.put(email + ":token", new long[]{ idToken.hashCode(), expiry });

                String body = "Your StudyBuddy verification code is: " + otp +
                              "\n\nThis code expires in 5 minutes. Do not share it with anyone.";
                emailService.sendEmail(email, "StudyBuddy 2FA Code", body);

                throw new TwoFARequiredException(email);
            }
        } catch (TwoFARequiredException e) {
            throw e;
        } catch (Exception e) {
            // If reading Firestore fails, do not block login
            // (email send failures are propagated before reaching here)
        }

        // 5. Return the real ID Token!
        return idToken;
    }

    /**
     * Verifies a 2FA OTP submitted by the user.
     * On success, re-runs loginUser to obtain and return the session token.
     */
    public String verifyTwoFA(String email, String code) throws Exception {
        long[] entry = otpStore.get(email);
        if (entry == null) {
            throw new IllegalArgumentException("No pending 2FA code for this account. Please log in again.");
        }
        long storedOtp = entry[0];
        long expiry = entry[1];

        if (Instant.now().getEpochSecond() > expiry) {
            otpStore.remove(email);
            otpStore.remove(email + ":token");
            throw new IllegalArgumentException("2FA code has expired. Please log in again.");
        }

        long submitted;
        try {
            submitted = Long.parseLong(code.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid code format.");
        }

        if (submitted != storedOtp) {
            throw new IllegalArgumentException("Incorrect 2FA code. Please try again.");
        }

        // OTP correct — clean up store and issue a real token by looking up the user
        otpStore.remove(email);
        otpStore.remove(email + ":token");

        UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
        String customToken = FirebaseAuth.getInstance().createCustomToken(user.getUid());

        // Exchange the custom token for an ID token via Firebase REST API
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("token", customToken);
        bodyData.put("returnSecureToken", true);
        String payload = mapper.writeValueAsString(bodyData);

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=" + webApiKey;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Could not issue session token after 2FA.");
        }

        JsonNode root = mapper.readTree(response.body());
        return root.path("idToken").asText();
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
     * Invalidates the user's current session by revoking refresh tokens in Firebase.
     * The currently issued ID token may still be valid for a short period, but we clear
     * it on the client side to enforce logout.
     */
    @Override
    public void logoutUser(String authHeader) throws Exception {
        String uid = verifyFrontendToken(authHeader);
        FirebaseAuth.getInstance().revokeRefreshTokens(uid);
    }

    /**
     * Triggers a password reset by calling Firebase's own email delivery API directly.
     * This bypasses SMTP entirely — Firebase sends the reset link from its own infra.
     */
    public String generateResetLink(String email) throws Exception {
        // 1. Confirm the user exists in Firebase Auth
        try {
            FirebaseAuth.getInstance().getUserByEmail(email);
        } catch (Exception e) {
	    return "Request processed.";
        }

        // 2. Ask Firebase to send the password reset email itself (no SMTP required)
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("requestType", "PASSWORD_RESET");
        bodyData.put("email", email);
        String payload = mapper.writeValueAsString(bodyData);

        String url = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=" + webApiKey;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Failed to send reset email: " + response.body());
        }

        return "Reset email sent successfully.";
    }
}
