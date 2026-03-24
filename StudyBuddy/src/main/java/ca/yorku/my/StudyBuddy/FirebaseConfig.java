package ca.yorku.my.StudyBuddy;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("firestore")
/**
 * This class initializes Firebase Admin SDK once during application startup.
 *
 * Priority order:
 * 1) FIREBASE_CREDENTIALS environment variable
 * 2) serviceAccountKey.json from classpath (This is our local development fallback)
 */
public class FirebaseConfig {

    /**
     * This helps bootstrap FirebaseApp if no existing app has been created.
     */
    @PostConstruct
    public void initialize() {
        try {

	        // Prefer runtime-provided credentials for containerized/cloud environments.
        	String envKey = System.getenv("FIREBASE_CREDENTIALS");
        	if (envKey != null && !envKey.isEmpty()) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(envKey.getBytes())
                        ))
                        .build();
                    
                    FirebaseApp.initializeApp(options);
                    System.out.println("Firebase initialized via Environment Variable.");
                }
                return; 
        	}

	        // Local fallback used for development only.
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    throw new IOException("serviceAccountKey.json not found in resources");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}