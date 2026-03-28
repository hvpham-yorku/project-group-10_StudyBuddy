package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.services.StubEventRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

class EventServiceTests {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StubEventRepository eventRepository;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        StubDatabase.EVENTS.clear();
    }

    @AfterEach
    void cleanup() throws Exception {
        StubDatabase.EVENTS.clear();
        mocks.close();
    }

    /**
     * tests if creating an event actually generates an ID and shoves it into our stub database.
     */
    @Test
    void createEvent_addsEventToStubDb() throws Exception {
        Event newEvent = new Event();
        newEvent.setTitle("Test Event");

        Event created = eventRepository.createEvent(newEvent);

        assertNotNull(created.getId());
        assertTrue(created.getId().startsWith("stub_"));
        assertEquals(1, StubDatabase.EVENTS.size());
        assertEquals("Test Event", StubDatabase.EVENTS.get(0).getTitle());
    }

    /**
     * tests if we can grab all the events currently sitting in the stub database.
     */
    @Test
    void getAllEvents_returnsEverything() throws Exception {
        Event e1 = new Event(); e1.setId("e1");
        Event e2 = new Event(); e2.setId("e2");
        StubDatabase.EVENTS.add(e1);
        StubDatabase.EVENTS.add(e2);

        List<Event> results = eventRepository.getAllEvents();

        assertEquals(2, results.size());
    }

    /**
     * tests grabbing a specific event by its ID. should return the exact object.
     */
    @Test
    void getEventById_findsExistingEvent() throws Exception {
        Event e = new Event();
        e.setId("target_id");
        e.setTitle("Find Me");
        StubDatabase.EVENTS.add(e);

        Event found = eventRepository.getEventById("target_id");

        assertNotNull(found);
        assertEquals("Find Me", found.getTitle());
    }

    /**
     * tests that we get a clean null if we try to fetch an event ID that doesn't exist.
     */
    @Test
    void getEventById_returnsNullWhenNotFound() throws Exception {
        Event found = eventRepository.getEventById("fake_id");
        assertNull(found);
    }

    /**
     * tests if the host can actually delete their own event.
     */
    @Test
    void deleteEvent_removesEventIfHostMatches() throws Exception {
        Event e = new Event();
        e.setId("delete_me");
        e.setHost("user123");
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.deleteEvent("delete_me", "user123");

        assertTrue(result);
        assertTrue(StubDatabase.EVENTS.isEmpty()); // Should be gone!
    }

    /**
     * tests to make sure random users can't delete an event they don't own.
     */
    @Test
    void deleteEvent_failsIfUserIsNotHost() throws Exception {
        Event e = new Event();
        e.setId("delete_me");
        e.setHost("user123");
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.deleteEvent("delete_me", "hacker99");

        assertFalse(result);
        assertEquals(1, StubDatabase.EVENTS.size()); // Event should survive
    }

    /**
     * updates the student's attended list and the event's attendee list.
     */
    @Test
    void joinEvent_updatesBothStudentAndEvent() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setAttendedEventIds(new ArrayList<>());
        when(studentRepository.getStudent("student1")).thenReturn(mockStudent);

        Event e = new Event();
        e.setId("event1");
        e.setAttendees(new ArrayList<>());
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.joinEvent("student1", "event1");

        assertTrue(result);
        assertTrue(e.getAttendees().contains("student1")); // Added to event
        verify(studentRepository).updateAttendedEventIDs(eq("student1"), anyList()); // Student DB update was triggered
    }

    /**
     * test leaving an event. should remove from both lists.
     */
    @Test
    void leaveEvent_removesFromBothStudentAndEvent() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setAttendedEventIds(new ArrayList<>(List.of("event1")));
        when(studentRepository.getStudent("student1")).thenReturn(mockStudent);

        Event e = new Event();
        e.setId("event1");
        e.setAttendees(new ArrayList<>(List.of("student1")));
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.leaveEvent("student1", "event1");

        assertTrue(result);
        assertFalse(e.getAttendees().contains("student1"));
        verify(studentRepository).updateAttendedEventIDs(eq("student1"), anyList());
    }

    /**
     * tests the internal helper to make sure it safely adds an attendee to an event's array list.
     */
    @Test
    void addAttendee_worksCorrectly() throws Exception {
        Event e = new Event();
        e.setId("event1");
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.addAttendee("event1", "student1");

        assertTrue(result);
        assertEquals(1, e.getAttendees().size());
        assertEquals("student1", e.getAttendees().get(0));
    }

    /**
     * tests the internal helper to ensure it drops an attendee from the event's array list.
     */
    @Test
    void removeAttendee_worksCorrectly() throws Exception {
        Event e = new Event();
        e.setId("event1");
        e.setAttendees(new ArrayList<>(List.of("student_a", "student_b")));
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.removeAttendee("event1", "student_a");

        assertTrue(result);
        assertEquals(1, e.getAttendees().size());
        assertFalse(e.getAttendees().contains("student_a"));
    }

    /**
     * tests that creating multiple events each gets a unique stub ID.
     */
    @Test
    void createEvent_multipleEventsGetUniqueIds() throws Exception {
        Event e1 = new Event(); e1.setTitle("Event One");
        Event e2 = new Event(); e2.setTitle("Event Two");

        Event created1 = eventRepository.createEvent(e1);
        Event created2 = eventRepository.createEvent(e2);

        assertNotEquals(created1.getId(), created2.getId());
        assertEquals(2, StubDatabase.EVENTS.size());
    }

    /**
     * tests that getAllEvents returns an empty list when the stub database has no events.
     */
    @Test
    void getAllEvents_returnsEmptyListWhenNoEvents() throws Exception {
        List<Event> results = eventRepository.getAllEvents();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * tests that deleting a non-existent event returns false without throwing.
     */
    @Test
    void deleteEvent_returnsFalseWhenEventDoesNotExist() throws Exception {
        boolean result = eventRepository.deleteEvent("nonexistent_id", "user123");

        assertFalse(result);
        assertTrue(StubDatabase.EVENTS.isEmpty());
    }

    /**
     * tests that joining an event that does not exist returns false.
     */
    @Test
    void joinEvent_returnsFalseWhenEventNotFound() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setAttendedEventIds(new ArrayList<>());
        when(studentRepository.getStudent("student1")).thenReturn(mockStudent);

        boolean result = eventRepository.joinEvent("student1", "ghost_event");

        assertFalse(result);
    }

    /**
     * tests that leaving an event that does not exist returns false.
     */
    @Test
    void leaveEvent_returnsFalseWhenEventNotFound() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setAttendedEventIds(new ArrayList<>(List.of("ghost_event")));
        when(studentRepository.getStudent("student1")).thenReturn(mockStudent);

        boolean result = eventRepository.leaveEvent("student1", "ghost_event");

        assertFalse(result);
    }

    /**
     * tests that a student who already joined cannot join the same event twice
     * (attendees list should not contain duplicates).
     */
    @Test
    void joinEvent_doesNotAddDuplicateAttendee() throws Exception {
        Student mockStudent = new Student();
        mockStudent.setAttendedEventIds(new ArrayList<>(List.of("event1")));
        when(studentRepository.getStudent("student1")).thenReturn(mockStudent);

        Event e = new Event();
        e.setId("event1");
        e.setAttendees(new ArrayList<>(List.of("student1"))); // already in
        StubDatabase.EVENTS.add(e);

        eventRepository.joinEvent("student1", "event1");

        long count = e.getAttendees().stream().filter(a -> a.equals("student1")).count();
        assertEquals(1, count);
    }

    /**
     * tests addAttendee on an event that does not exist returns false.
     */
    @Test
    void addAttendee_returnsFalseWhenEventNotFound() throws Exception {
        boolean result = eventRepository.addAttendee("nonexistent_event", "student1");

        assertFalse(result);
    }

    /**
     * tests removeAttendee on an event that does not exist returns false.
     */
    @Test
    void removeAttendee_returnsFalseWhenEventNotFound() throws Exception {
        boolean result = eventRepository.removeAttendee("nonexistent_event", "student1");

        assertFalse(result);
    }

    /**
     * tests that addAttendee on an event with a null attendees list
     * initializes the list and successfully adds the attendee.
     */
    @Test
    void addAttendee_handlesNullAttendeesList() throws Exception {
        Event e = new Event();
        e.setId("event_null_list");
        e.setAttendees(null); // explicitly null
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.addAttendee("event_null_list", "student1");

        assertTrue(result);
        assertNotNull(e.getAttendees());
        assertTrue(e.getAttendees().contains("student1"));
    }

    /**
     * tests that the host of an event can delete it even when other attendees are present.
     */
    @Test
    void deleteEvent_hostCanDeleteEventWithAttendees() throws Exception {
        Event e = new Event();
        e.setId("busy_event");
        e.setHost("organizer");
        e.setAttendees(new ArrayList<>(List.of("student_a", "student_b", "student_c")));
        StubDatabase.EVENTS.add(e);

        boolean result = eventRepository.deleteEvent("busy_event", "organizer");

        assertTrue(result);
        assertTrue(StubDatabase.EVENTS.isEmpty());
    }
}