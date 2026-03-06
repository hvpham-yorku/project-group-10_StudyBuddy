package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.services.ConnectionsService;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import com.google.api.core.ApiFuture;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConnectionsService.
 * Verifies accepted connections retrieval logic with Firestore mocks.
 */
@ExtendWith(MockitoExtension.class)
public class ConnectionsServiceUnitTests {
    @Test
    void getAcceptedConnections_FirestoreThrows_ReturnsEmpty() throws Exception, InterruptedException {
        lenient().when(q1Future.get()).thenThrow(new RuntimeException("Firestore error"));
        lenient().when(q2Future.get()).thenThrow(new RuntimeException("Firestore error"));

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            assertThrows(RuntimeException.class, () -> connectionsService.getAcceptedConnections("u1"));
        }
    }

    @Test
    void getAcceptedConnections_NoConnections_ReturnsEmpty() throws Exception, InterruptedException {
        when(q1Snapshot.getDocuments()).thenReturn(Collections.emptyList());
        when(q2Snapshot.getDocuments()).thenReturn(Collections.emptyList());

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            List<ConnectionsService.ConnectionDTO> result = connectionsService.getAcceptedConnections("u1");
            assertTrue(result.isEmpty());
        }
    }

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference connectionsCollection;

    @Mock
    private Query q1, q2;

    @Mock
    private ApiFuture<QuerySnapshot> q1Future, q2Future;

    @Mock
    private QuerySnapshot q1Snapshot, q2Snapshot;

    @Mock
    private QueryDocumentSnapshot docA, docB;

    @InjectMocks
    private ConnectionsService connectionsService;

    @BeforeEach
    void setUp() {
        lenient().when(firestore.collection("connections")).thenReturn(connectionsCollection);
        lenient().when(connectionsCollection.whereEqualTo(anyString(), anyString())).thenReturn(q1, q2);
        lenient().when(q1.whereEqualTo(anyString(), anyString())).thenReturn(q1);
        lenient().when(q2.whereEqualTo(anyString(), anyString())).thenReturn(q2);
        lenient().when(q1.get()).thenReturn(q1Future);
        lenient().when(q2.get()).thenReturn(q2Future);
        try {
            lenient().when(q1Future.get()).thenReturn(q1Snapshot);
            lenient().when(q2Future.get()).thenReturn(q2Snapshot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // ignore for test setup
        }
    }

    /**
     * Test getAcceptedConnections returns correct DTOs for valid user.
     */
    @Test
    void getAcceptedConnections_ValidUser_ReturnsConnections() throws Exception, InterruptedException {
        List<QueryDocumentSnapshot> docsA = Arrays.asList(docA);
        List<QueryDocumentSnapshot> docsB = Arrays.asList(docB);
        when(q1Snapshot.getDocuments()).thenReturn(docsA);
        when(q2Snapshot.getDocuments()).thenReturn(docsB);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            List<ConnectionsService.ConnectionDTO> result = connectionsService.getAcceptedConnections("u1");
            assertNotNull(result);
        }
    }

    /**
     * Test getAcceptedConnections returns empty for null/blank user.
     */
    @Test
    void getAcceptedConnections_NullOrBlankUser_ReturnsEmpty() {
        List<ConnectionsService.ConnectionDTO> result1 = connectionsService.getAcceptedConnections(null);
        List<ConnectionsService.ConnectionDTO> result2 = connectionsService.getAcceptedConnections("");
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }
}
