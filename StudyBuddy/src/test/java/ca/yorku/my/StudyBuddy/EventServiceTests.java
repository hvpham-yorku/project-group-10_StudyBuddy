package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.services.StubEventRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
}