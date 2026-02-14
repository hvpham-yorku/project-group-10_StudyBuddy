package ca.yorku.my.StudyBuddy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for Event-related functionality.
 * Tests both the Event model methods and the EventController endpoints.
 * Uses MockMvc to simulate HTTP requests without starting the full server.
 */
@WebMvcTest(EventController.class)
class EventTests {

    @Autowired
    private MockMvc mockMvc;

    // Mock the EventService so we don't need Firebase for testing
    @MockBean
    private EventService eventService;

    private Event testEvent;
    private List<String> testParticipants;

    /**
     * Set up test data before each test.
     * Creates a sample event and participant list for testing.
     */
    @BeforeEach
    void setUp() {
        // Create test participants list
        testParticipants = new ArrayList<>(Arrays.asList("user1", "user2", "user3"));
        
        // Create a test event with all fields
        testEvent = new Event(
            "test_host_1",
            "Study Session for EECS 2311",
            "EECS 2311",
            "Scott Library Room 101",
            "Let's review for the midterm together!",
            "2026-02-20 14:00",
            "2026-02-20 16:00",
            5
        );
        testEvent.setEventId("test_event_123");
        testEvent.setParticipantIds(new ArrayList<>(testParticipants));
    }

    // ==================== UNIT TESTS FOR EVENT MODEL ====================

    /**
     * Test that Event constructor properly initializes all fields.
     */
    @Test
    void testEventConstructor() {
        Event event = new Event(
            "host123",
            "Java Study Group",
            "EECS 2030",
            "TEL Building",
            "Practice coding problems",
            "2026-03-01 10:00",
            "2026-03-01 12:00",
            10
        );

        assert event.getHostId().equals("host123");
        assert event.getTitle().equals("Java Study Group");
        assert event.getCourse().equals("EECS 2030");
        assert event.getLocation().equals("TEL Building");
        assert event.getDescription().equals("Practice coding problems");
        assert event.getStartTime().equals("2026-03-01 10:00");
        assert event.getEndTime().equals("2026-03-01 12:00");
        assert event.getMaxCapacity() == 10;
        assert event.getParticipantIds().isEmpty(); // Should start empty
    }

    /**
     * Test adding participants to an event.
     */
    @Test
    void testAddParticipant() {
        Event event = new Event(
            "host1",
            "Test Event",
            "EECS 1001",
            "Online",
            "Test description",
            "2026-02-15 09:00",
            "2026-02-15 11:00",
            3
        );

        // Initially no participants
        assert event.getParticipantIds().size() == 0;

        // Add participant
        event.getParticipantIds().add("user1");
        assert event.getParticipantIds().size() == 1;
        assert event.getParticipantIds().contains("user1");
    }

    /**
     * Test removing participants from an event.
     */
    @Test
    void testRemoveParticipant() {
        Event event = new Event(
            "host1",
            "Test Event",
            "EECS 1001",
            "Online",
            "Test description",
            "2026-02-15 09:00",
            "2026-02-15 11:00",
            3
        );

        // Add some participants
        event.getParticipantIds().add("user1");
        event.getParticipantIds().add("user2");
        assert event.getParticipantIds().size() == 2;

        // Remove a participant
        event.removeParticipant("user1");
        assert event.getParticipantIds().size() == 1;
        assert !event.getParticipantIds().contains("user1");
        assert event.getParticipantIds().contains("user2");

        // Try to remove non-existent participant (should handle gracefully)
        event.removeParticipant("user999");
        assert event.getParticipantIds().size() == 1; // Size unchanged
    }

    /**
     * Test all getter and setter methods.
     */
    @Test
    void testGettersAndSetters() {
        Event event = new Event();
        
        event.setEventId("id123");
        event.setHostId("host456");
        event.setTitle("New Title");
        event.setCourse("EECS 3311");
        event.setLocation("Bethune College");
        event.setDescription("New Description");
        event.setStartTime("2026-04-01 13:00");
        event.setEndTime("2026-04-01 15:00");
        event.setMaxCapacity(20);
        
        List<String> participants = Arrays.asList("p1", "p2");
        event.setParticipantIds(participants);

        assert event.getEventId().equals("id123");
        assert event.getHostId().equals("host456");
        assert event.getTitle().equals("New Title");
        assert event.getCourse().equals("EECS 3311");
        assert event.getLocation().equals("Bethune College");
        assert event.getDescription().equals("New Description");
        assert event.getStartTime().equals("2026-04-01 13:00");
        assert event.getEndTime().equals("2026-04-01 15:00");
        assert event.getMaxCapacity() == 20;
        assert event.getParticipantIds().size() == 2;
    }

    // ==================== API TESTS FOR EVENT CONTROLLER ====================

    /**
     * Test POST /api/events - Creating a new event.
     * Should return 201 CREATED with the event including auto-generated ID.
     */
    @Test
    void testCreateEvent() throws Exception {
        // Mock the service to return our test event when createEvent is called
        when(eventService.createEvent(any(Event.class))).thenReturn(testEvent);

        // JSON representing the event to create (without eventId)
        String eventJson = "{"
            + "\"hostId\":\"test_host_1\","
            + "\"title\":\"Study Session for EECS 2311\","
            + "\"course\":\"EECS 2311\","
            + "\"location\":\"Scott Library Room 101\","
            + "\"description\":\"Let's review for the midterm together!\","
            + "\"startTime\":\"2026-02-20 14:00\","
            + "\"endTime\":\"2026-02-20 16:00\","
            + "\"maxCapacity\":5"
            + "}";

        // Perform POST request and verify response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.eventId").value("test_event_123"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hostId").value("test_host_1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Study Session for EECS 2311"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.course").value("EECS 2311"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.maxCapacity").value(5));
    }

    /**
     * Test GET /api/events - Retrieving all events.
     * Should return 200 OK with a list of events.
     */
    @Test
    void testGetAllEvents() throws Exception {
        // Create a second test event
        Event event2 = new Event(
            "test_host_2",
            "Database Design Workshop",
            "EECS 3421",
            "ACE Building",
            "Learn about normalization",
            "2026-02-25 10:00",
            "2026-02-25 12:00",
            10
        );
        event2.setEventId("test_event_456");

        // Mock the service to return a list of events
        List<Event> events = Arrays.asList(testEvent, event2);
        when(eventService.getAllEvents()).thenReturn(events);

        // Perform GET request and verify response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/events"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventId").value("test_event_123"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Study Session for EECS 2311"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventId").value("test_event_456"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value("Database Design Workshop"));
    }

    /**
     * Test GET /api/events - When there are no events.
     * Should return 200 OK with an empty array.
     */
    @Test
    void testGetAllEventsEmpty() throws Exception {
        // Mock the service to return empty list
        when(eventService.getAllEvents()).thenReturn(new ArrayList<>());

        // Perform GET request and verify empty response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/events"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    /**
     * Test DELETE /api/events/{id}?userId={userId} - Successful deletion.
     * Should return 204 NO CONTENT when authorized user deletes event.
     */
    @Test
    void testDeleteEventSuccess() throws Exception {
        // Mock the service to return true (deletion successful)
        when(eventService.deleteEvent(eq("test_event_123"), eq("test_host_1")))
            .thenReturn(true);

        // Perform DELETE request with correct userId
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/events/test_event_123")
                .param("userId", "test_host_1"))
            .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Test DELETE /api/events/{id}?userId={userId} - Unauthorized deletion.
     * Should return 403 FORBIDDEN when non-host tries to delete event.
     */
    @Test
    void testDeleteEventForbidden() throws Exception {
        // Mock the service to return false (deletion failed - not authorized)
        when(eventService.deleteEvent(eq("test_event_123"), eq("wrong_user")))
            .thenReturn(false);

        // Perform DELETE request with wrong userId
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/events/test_event_123")
                .param("userId", "wrong_user"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    /**
     * Test DELETE /api/events/{id}?userId={userId} - Event doesn't exist.
     * Should return 403 FORBIDDEN when trying to delete non-existent event.
     */
    @Test
    void testDeleteEventNotFound() throws Exception {
        // Mock the service to return false (event doesn't exist)
        when(eventService.deleteEvent(eq("nonexistent_id"), eq("test_host_1")))
            .thenReturn(false);

        // Perform DELETE request for non-existent event
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/events/nonexistent_id")
                .param("userId", "test_host_1"))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}