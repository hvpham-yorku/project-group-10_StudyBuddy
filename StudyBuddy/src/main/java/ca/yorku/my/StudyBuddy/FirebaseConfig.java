
package ca.yorku.my.StudyBuddy;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;



//*** 
// 
// This class is can be used to configure Firebase settings.
// Runs once during startup to initialize Firebase Admin SDK with service account credentials.
// Loads serviceAccountKey.json from src/main/resources/
// Initializes Firebase Admin SDK
// 
//  

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
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