package ca.yorku.my.StudyBuddy.unit;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.services.AuthService;
import ca.yorku.my.StudyBuddy.services.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginStreakUnitTests {

    @InjectMocks
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserRecord userRecord;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private ApiFuture<DocumentSnapshot> documentFuture;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<WriteResult> writeFuture;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "webApiKey", "test-key");
    }

    @Test
    void loginUser_consecutiveDayIncrementsStreak() throws Exception {
        String email = "test@my.yorku.ca";
        String uid = "u1";
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        String token = loginWithMockedDependencies(email, "password", uid, yesterday, 3L);

        assertEquals("mockIdToken", token);
        verify(documentReference).update("loginStreak", 4, "lastLoginDate", today);
    }

    @Test
    void loginUser_sameDayDoesNotUpdateStreak() throws Exception {
        String email = "test@my.yorku.ca";
        String uid = "u1";
        String today = LocalDate.now().toString();

        String token = loginWithMockedDependencies(email, "password", uid, today, 7L);

        assertEquals("mockIdToken", token);
        verify(documentReference, never()).update(any(String.class), any(), any(String.class), any());
    }

    @Test
    void loginUser_gapResetsStreakToOne() throws Exception {
        String email = "test@my.yorku.ca";
        String uid = "u1";
        String today = LocalDate.now().toString();
        String olderDate = LocalDate.now().minusDays(4).toString();

        String token = loginWithMockedDependencies(email, "password", uid, olderDate, 9L);

        assertEquals("mockIdToken", token);
        verify(documentReference).update("loginStreak", 1, "lastLoginDate", today);
    }

    @Test
    void loginUser_unverifiedEmailThrowsBeforeStreakUpdate() throws Exception {
        String email = "test@my.yorku.ca";
        String password = "password";

        try (MockedStatic<HttpClient> httpClientStatic = org.mockito.Mockito.mockStatic(HttpClient.class);
             MockedStatic<FirebaseAuth> firebaseAuthStatic = org.mockito.Mockito.mockStatic(FirebaseAuth.class)) {

            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("{\"idToken\":\"mockIdToken\"}");

            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.getUserByEmail(email)).thenReturn(userRecord);
            when(userRecord.isEmailVerified()).thenReturn(false);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> authService.loginUser(email, password));
            assertEquals("Login Denied: Please verify your YorkU email first.", ex.getMessage());
        }
    }

    private String loginWithMockedDependencies(String email, String password, String uid, String lastLoginDate, Long streak) throws Exception {
        try (MockedStatic<HttpClient> httpClientStatic = org.mockito.Mockito.mockStatic(HttpClient.class);
             MockedStatic<FirebaseAuth> firebaseAuthStatic = org.mockito.Mockito.mockStatic(FirebaseAuth.class);
             MockedStatic<FirestoreClient> firestoreClientStatic = org.mockito.Mockito.mockStatic(FirestoreClient.class)) {

            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(httpClient);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("{\"idToken\":\"mockIdToken\"}");

            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.getUserByEmail(email)).thenReturn(userRecord);
            when(userRecord.isEmailVerified()).thenReturn(true);
            when(userRecord.getUid()).thenReturn(uid);

            firestoreClientStatic.when(FirestoreClient::getFirestore).thenReturn(firestore);
            when(firestore.collection("students")).thenReturn(collectionReference);
            when(collectionReference.document(uid)).thenReturn(documentReference);
            when(documentReference.get()).thenReturn(documentFuture);
            when(documentFuture.get()).thenReturn(documentSnapshot);
            when(documentSnapshot.exists()).thenReturn(true);
            when(documentSnapshot.getString("lastLoginDate")).thenReturn(lastLoginDate);
            when(documentSnapshot.getLong("loginStreak")).thenReturn(streak);
            lenient().when(documentReference.update(eq("loginStreak"), any(), eq("lastLoginDate"), any())).thenReturn(writeFuture);
            lenient().when(writeFuture.get()).thenReturn(null);

            return authService.loginUser(email, password);
        }
    }
}
