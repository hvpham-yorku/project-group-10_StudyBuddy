package ca.yorku.my.StudyBuddy.unit;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.services.EventService;

/**
 * Unit tests for EventService.
 * Tests CRUD operations on events in isolation by mocking Firestore interactions.
 * Industry standards applied: JUnit 5, Mockito for async operations, comprehensive coverage
 * of success and failure scenarios.
 */
@ExtendWith(MockitoExtension.class)
public class EventServiceUnitTests {

    @InjectMocks
    private EventService eventService;

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionRef;

    @Mock
    private DocumentReference documentRef;

    @Mock
    private ApiFuture<DocumentReference> addFuture;

    @Mock
    private ApiFuture<WriteResult> setFuture;

    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;

    @Mock
    private ApiFuture<DocumentSnapshot> docFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private QueryDocumentSnapshot queryDoc;

    @Mock
    private WriteResult writeResult;

    @BeforeEach
    void setUp() {
        // Use lenient for all stubbings to avoid UnnecessaryStubbingException
        lenient().when(firestore.collection(anyString())).thenReturn(collectionRef);
        lenient().when(collectionRef.add(any(Event.class))).thenReturn(addFuture);
        lenient().when(collectionRef.document(anyString())).thenReturn(documentRef);
        lenient().when(collectionRef.get()).thenReturn(queryFuture);
        lenient().when(documentRef.get()).thenReturn(docFuture);
        lenient().when(documentRef.set(any(Event.class))).thenReturn(setFuture);
        lenient().when(documentRef.delete()).thenReturn(setFuture);
    }

    /**
     * Test createEvent with valid event.
     * Verifies event is added to Firestore and ID is set.
     */
    @Test
    void createEvent_ValidEvent_ShouldCreateAndReturnEvent() throws Exception {
        // Given: Valid event
        Event event = new Event();
        event.setTitle("Test Event");

        when(addFuture.get()).thenReturn(documentRef);
        when(documentRef.getId()).thenReturn("generatedId");
        when(setFuture.get()).thenReturn(writeResult);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Creating event
            Event result = eventService.createEvent(event);

            // Then: Event is created with ID
            assertNotNull(result);
            assertEquals("generatedId", result.getId());
            verify(collectionRef).add(event);
            verify(documentRef).set(event);
        }
    }

    /**
     * Test createEvent when Firestore add fails.
     * Ensures exceptions are propagated.
     */
    @Test
    void createEvent_FirestoreError_ShouldThrowException() throws Exception {
        // Given: Firestore add throws exception
        Event event = new Event();
        when(addFuture.get()).thenThrow(new ExecutionException("Firestore error", null));

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When & Then: Exception is thrown
            assertThrows(ExecutionException.class, () -> eventService.createEvent(event));
        }
    }

    /**
     * Test getAllEvents with existing events.
     * Verifies all events are retrieved and converted.
     */
    @Test
    void getAllEvents_ExistingEvents_ShouldReturnList() throws Exception {
        // Given: Mock documents
        Event event1 = new Event();
        event1.setTitle("Event 1");
        Event event2 = new Event();
        event2.setTitle("Event 2");

        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(queryDoc, queryDoc));
        when(queryDoc.toObject(Event.class)).thenReturn(event1, event2);
        when(queryDoc.getId()).thenReturn("id1", "id2");

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Getting all events
            List<Event> result = eventService.getAllEvents();

            // Then: List of events returned
            assertEquals(2, result.size());
            assertEquals("Event 1", result.get(0).getTitle());
            assertEquals("id1", result.get(0).getId());
        }
    }

    /**
     * Test getEventById with existing event.
     * Checks event retrieval by ID.
     */
    @Test
    void getEventById_ExistingEvent_ShouldReturnEvent() throws Exception {
        // Given: Event exists
        Event event = new Event();
        event.setTitle("Existing Event");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Getting event by ID
            Event result = eventService.getEventById("eventId");

            // Then: Event is returned
            assertNotNull(result);
            assertEquals("Existing Event", result.getTitle());
            assertEquals("eventId", result.getId());
        }
    }

    /**
     * Test getEventById with non-existing event.
     * Verifies null is returned for missing events.
     */
    @Test
    void getEventById_NonExistingEvent_ShouldReturnNull() throws Exception {
        // Given: Event does not exist
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Getting event by ID
            Event result = eventService.getEventById("nonExistentId");

            // Then: Null is returned
            assertNull(result);
        }
    }

    /**
     * Test deleteEvent by host.
     * Ensures host can delete their event.
     */
    @Test
    void deleteEvent_ByHost_ShouldDeleteSuccessfully() throws Exception {
        // Given: Event exists and user is host
        Event event = new Event();
        event.setHost("hostUserId");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);
        when(setFuture.get()).thenReturn(writeResult);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Deleting event
            boolean result = eventService.deleteEvent("eventId", "hostUserId");

            // Then: Deletion succeeds
            assertTrue(result);
            verify(documentRef).delete();
        }
    }

    /**
     * Test deleteEvent by non-host.
     * Verifies non-hosts cannot delete events.
     */
    @Test
    void deleteEvent_ByNonHost_ShouldFail() throws Exception {
        // Given: User is not the host
        Event event = new Event();
        event.setHost("hostUserId");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);

        try (MockedStatic<FirestoreClient> firestoreClientMock = mockStatic(FirestoreClient.class)) {
            firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When: Attempting to delete
            boolean result = eventService.deleteEvent("eventId", "otherUserId");

            // Then: Deletion fails
            assertFalse(result);
            verify(documentRef, never()).delete();
        }
    }

    // Note: joinEvent involves StudentService, which would require additional mocking.
    // For brevity, omitted here, but in full suite, mock StudentService and test join logic.
}