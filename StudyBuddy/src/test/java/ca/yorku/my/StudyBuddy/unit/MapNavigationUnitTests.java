package ca.yorku.my.StudyBuddy.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Unit tests covering the logic that supports:
 *   1. Viewing session pins on the map  (getAllEvents, getEventById, location filtering)
 *   2. Navigation to a study session    (getEventById for coords, addAttendee, deleteEvent auth)
 *
 * Follows the same JUnit 5 + Mockito pattern as EventServiceUnitTests.java.
 */
@ExtendWith(MockitoExtension.class)
public class MapNavigationUnitTests {

    @InjectMocks
    private EventService eventService;

    @Mock private Firestore firestore;
    @Mock private CollectionReference collectionRef;
    @Mock private DocumentReference documentRef;
    @Mock private ApiFuture<DocumentReference> addFuture;
    @Mock private ApiFuture<WriteResult> setFuture;
    @Mock private ApiFuture<QuerySnapshot> queryFuture;
    @Mock private ApiFuture<DocumentSnapshot> docFuture;
    @Mock private QuerySnapshot querySnapshot;
    @Mock private DocumentSnapshot documentSnapshot;
    @Mock private QueryDocumentSnapshot queryDoc;
    @Mock private WriteResult writeResult;

    @BeforeEach
    void setUp() {
        lenient().when(firestore.collection(anyString())).thenReturn(collectionRef);
        lenient().when(collectionRef.add(any(Event.class))).thenReturn(addFuture);
        lenient().when(collectionRef.document(anyString())).thenReturn(documentRef);
        lenient().when(collectionRef.get()).thenReturn(queryFuture);
        lenient().when(documentRef.get()).thenReturn(docFuture);
        lenient().when(documentRef.set(any(Event.class))).thenReturn(setFuture);
        lenient().when(documentRef.delete()).thenReturn(setFuture);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-01  getAllEvents returns all events so the map can render every pin
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Verifies getAllEvents returns the full list of events.
     * The map needs every event to decide which building pins to draw.
     */
    @Test
    void getAllEvents_MultipleEvents_ReturnsFullListForMapPins() throws Exception {
        // Given: Three events stored in Firestore
        Event e1 = new Event(); e1.setTitle("EECS Cram"); e1.setLocation("Scott Library");
        Event e2 = new Event(); e2.setTitle("Math Group"); e2.setLocation("Vari Hall");
        Event e3 = new Event(); e3.setTitle("Physics Lab"); e3.setLocation("Bergeron Centre");

        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(queryDoc, queryDoc, queryDoc));
        when(queryDoc.toObject(Event.class)).thenReturn(e1, e2, e3);
        when(queryDoc.getId()).thenReturn("id1", "id2", "id3");

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            List<Event> result = eventService.getAllEvents();

            // Then: All three events are returned — map will draw three building pins
            assertEquals(3, result.size());
            assertEquals("EECS Cram",   result.get(0).getTitle());
            assertEquals("Math Group",  result.get(1).getTitle());
            assertEquals("Physics Lab", result.get(2).getTitle());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-02  getAllEvents with no events returns an empty list (map shows no pins)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * When there are no events in Firestore, the map should show zero pins.
     * Confirms getAllEvents returns an empty list rather than null.
     */
    @Test
    void getAllEvents_NoEvents_ReturnsEmptyListSoMapShowsNoPins() throws Exception {
        // Given: Empty Firestore collection
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(new ArrayList<>());

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            List<Event> result = eventService.getAllEvents();

            // Then: Empty list — map renders zero building pins
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-03  getEventById returns location field needed for navigation target
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * When a user clicks "Navigate Here", the frontend calls getEventById to
     * confirm the event exists and read its location before routing.
     */
    @Test
    void getEventById_ExistingEvent_ReturnsLocationForNavigation() throws Exception {
        // Given: Event at Scott Library
        Event event = new Event();
        event.setTitle("EECS 2311 Study");
        event.setLocation("Scott Library");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            Event result = eventService.getEventById("evt-001");

            // Then: Location is available for the navigation layer to use
            assertNotNull(result);
            assertEquals("Scott Library", result.getLocation());
            assertEquals("evt-001", result.getId());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-04  getEventById returns null for deleted/unknown event (cannot navigate)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Navigation should gracefully fail when the event no longer exists.
     * Confirms getEventById returns null so the frontend can show an error.
     */
    @Test
    void getEventById_NonExistingEvent_ReturnsNullPreventsNavigation() throws Exception {
        // Given: Document does not exist in Firestore
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            Event result = eventService.getEventById("ghost-id");

            // Then: null returned — frontend should not attempt navigation
            assertNull(result);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-05  Events with a location field are included in getAllEvents for pin rendering
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The map groups events by building name.
     * Confirms that events with a non-null location are returned
     * and the location value is preserved exactly.
     */
    @Test
    void getAllEvents_EventWithLocation_PreservesLocationForPinGrouping() throws Exception {
        // Given: Event with a known York building name
        Event event = new Event();
        event.setTitle("Data Structures Review");
        event.setLocation("Lassonde Building");

        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(queryDoc));
        when(queryDoc.toObject(Event.class)).thenReturn(event);
        when(queryDoc.getId()).thenReturn("evt-loc-01");

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            List<Event> result = eventService.getAllEvents();

            // Then: Location is intact so the frontend can match it to YORK_BUILDINGS
            assertEquals(1, result.size());
            assertEquals("Lassonde Building", result.get(0).getLocation());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-06  Host can delete event (remove its pin from the map)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * When a host deletes their event the building pin should disappear from the map.
     * Confirms deleteEvent returns true and calls Firestore delete for the host.
     */
    @Test
    void deleteEvent_ByHost_PinRemovedFromMap() throws Exception {
        // Given: Event exists and requesting user is host
        Event event = new Event();
        event.setHost("host-uid");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);
        when(setFuture.get()).thenReturn(writeResult);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            boolean deleted = eventService.deleteEvent("evt-del-01", "host-uid");

            // Then: Deletion succeeds — frontend removes the pin
            assertTrue(deleted);
            verify(documentRef).delete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-07  Non-host cannot delete event (pin stays on map)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Only the host should be able to remove an event pin.
     * Verifies that a different user's delete request is rejected.
     */
    @Test
    void deleteEvent_ByNonHost_PinRemainsOnMap() throws Exception {
        // Given: Requesting user is NOT the host
        Event event = new Event();
        event.setHost("host-uid");

        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(Event.class)).thenReturn(event);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            boolean deleted = eventService.deleteEvent("evt-del-01", "random-uid");

            // Then: Deletion rejected — pin stays visible for other students
            assertFalse(deleted);
            verify(documentRef, never()).delete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-08  deleteEvent returns false for non-existent event (no stale pins)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attempting to delete an event that doesn't exist should return false cleanly.
     * Prevents phantom pins appearing/disappearing due to bad state.
     */
    @Test
    void deleteEvent_NonExistingEvent_ReturnsFalseNoPinChange() throws Exception {
        // Given: Document does not exist
        when(docFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            boolean deleted = eventService.deleteEvent("ghost-event", "any-uid");

            // Then: False returned — no Firestore delete called
            assertFalse(deleted);
            verify(documentRef, never()).delete();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-09  getAllEvents returns attendees list so map info window shows capacity
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The map info window shows "X / maxParticipants attending".
     * Verifies attendees list is preserved through getAllEvents.
     */
    @Test
    void getAllEvents_EventWithAttendees_PreservesAttendeesForCapacityDisplay() throws Exception {
        // Given: Event with two attendees already joined
        Event event = new Event();
        event.setTitle("Calculus Cram");
        event.setMaxParticipants(5);
        event.setAttendees(Arrays.asList("student-1", "student-2"));

        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(queryDoc));
        when(queryDoc.toObject(Event.class)).thenReturn(event);
        when(queryDoc.getId()).thenReturn("evt-cap-01");

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            List<Event> result = eventService.getAllEvents();

            // Then: Attendee count is available for the info window capacity bar
            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getAttendees().size());
            assertEquals(5, result.get(0).getMaxParticipants());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-10  createEvent stores event and returns it with ID (pin appears on map)
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * When a student creates a new event via the Host form, it is saved to
     * Firestore and the returned object (with generated ID) is used to
     * immediately render a new pin on the map without a page refresh.
     */
    @Test
    void createEvent_ValidEvent_ReturnsSavedEventWithIdForImmediatePinRender() throws Exception {
        // Given: New event being created
        Event event = new Event();
        event.setTitle("Algorithm Review");
        event.setLocation("Ross Building");
        event.setHost("student-host");

        when(addFuture.get()).thenReturn(documentRef);
        when(documentRef.getId()).thenReturn("new-pin-id");
        when(setFuture.get()).thenReturn(writeResult);

        try (MockedStatic<FirestoreClient> mock = mockStatic(FirestoreClient.class)) {
            mock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            // When
            Event result = eventService.createEvent(event);

            // Then: Event is saved and ID is set — frontend can add pin immediately
            assertNotNull(result);
            assertEquals("new-pin-id", result.getId());
            assertEquals("Ross Building", result.getLocation());
            verify(collectionRef).add(event);
            verify(documentRef).set(event);
        }
    }
}