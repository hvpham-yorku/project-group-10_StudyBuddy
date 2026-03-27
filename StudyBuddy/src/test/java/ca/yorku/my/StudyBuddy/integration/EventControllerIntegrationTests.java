package ca.yorku.my.StudyBuddy.integration;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.EventResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stub") // Force it to use StubRepositories instead of Firestore
/**
 * The idea is that we want to simulate the HTTP requests while still using stub database, so as to isolate
 * Firebase issues and the backend issues.
 */
class EventControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Always start with a clean slate
        StubDatabase.EVENTS.clear();
        StubDatabase.STUDENTS.clear();
        
        Student dummyHost = new Student();
        dummyHost.setUserId("host_123");
        dummyHost.setFirstName("John");
        dummyHost.setLastName("Doe");
        StubDatabase.STUDENTS.add(dummyHost);
    }

    @AfterEach
    void cleanup() {
        StubDatabase.EVENTS.clear();
        StubDatabase.STUDENTS.clear();
    }

    /**
     * Tests if sending a GET request fetches the list of events and maps the HostDTO properly.
     */
    @Test
    void getAllEvents_returnsListOfEvents() throws Exception {
        // 1. Manually add an event to the stub database
        Event e = new Event();
        e.setId("evt_001");
        e.setTitle("Hackathon Prep");
        e.setHost("host_123"); // MUST match our dummy student in setup()
        StubDatabase.EVENTS.add(e);

        // 2. Perform HTTP GET
        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk()) // Expect HTTP 200
               .andExpect(jsonPath("$[0].id").value("evt_001"))
               .andExpect(jsonPath("$[0].title").value("Hackathon Prep"))
               .andExpect(jsonPath("$[0].host.name").value("John Doe")); // Verifies the HostDTO mapping worked!
    }

    /**
     * Tests joining an event via HTTP POST map payload.
     */
    @Test
    void joinEvent_returnsSuccessMessage() throws Exception {
        // 1. Setup an existing event
        Event e = new Event();
        e.setId("evt_001");
        e.setAttendees(new ArrayList<>()); // null safety
        StubDatabase.EVENTS.add(e);

        // 2. Setup an existing student so the repository doesn't crash!
        Student s = new Student();
        s.setUserId("student_99");
        s.setAttendedEventIds(new ArrayList<>()); // null safety
        StubDatabase.STUDENTS.add(s);

        // 3. Prepare the JSON payload
        String jsonPayload = "{\"userId\":\"student_99\", \"eventId\":\"evt_001\"}";

        // 4. Fire the HTTP request WITH the Authorization header
        mockMvc.perform(post("/api/events/join")
                .header("Authorization", "student_99") // <--- Added missing Auth header!
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
               .andExpect(status().isCreated())
               .andExpect(content().string("Joined Event!"));
    }

    /**
     * Tests that a Host can delete their own event.
     */
    @Test
    void deleteEvent_successReturnsNoContent() throws Exception {
        // Setup event
        Event e = new Event();
        e.setId("evt_delete_me");
        e.setHost("host_123");
        StubDatabase.EVENTS.add(e);

        // Fire DELETE request
        mockMvc.perform(delete("/api/events/evt_delete_me")
                .header("Authorization", "host_123")) // Authenticating as the host
               .andExpect(status().isNoContent()); // Expect HTTP 204 No Content
    }

    /**
     * Tests that the system prevents unauthorized users from deleting someone else's event.
     */
    @Test
    void deleteEvent_unauthorizedReturnsForbidden() throws Exception {
        // Setup event owned by someone else
        Event e = new Event();
        e.setId("evt_secure");
        e.setHost("host_123");
        StubDatabase.EVENTS.add(e);

        // Fire DELETE request as a different user
        mockMvc.perform(delete("/api/events/evt_secure")
                .header("Authorization", "hacker_99")) // Authenticating as wrong person
               .andExpect(status().isForbidden()); // Expect HTTP 403 Forbidden
    }
    
    /**
     * Tests fetching a single event by its ID. 
     * Ensures the Event and the Host details are mapped correctly into the EventResponseDTO.
     */
    @Test
    void getEvent_returnsSingleEventDTO() throws Exception {
        // 1. Setup Host Student (Using a unique ID!)
        Student host = new Student();
        host.setUserId("host_jane_99");
        host.setFirstName("Jane");
        host.setLastName("Doe");
        StubDatabase.STUDENTS.add(host);

        // 2. Setup Event
        Event e = new Event();
        e.setId("evt_single_lookup");
        e.setTitle("Quiet Study Time");
        e.setHost("host_jane_99");
        e.setLocation("Bronfman Library");
        StubDatabase.EVENTS.add(e);

        // 3. Fire GET request for that specific ID
        mockMvc.perform(get("/api/events/evt_single_lookup")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("evt_single_lookup"))
               .andExpect(jsonPath("$.title").value("Quiet Study Time"))
               .andExpect(jsonPath("$.location").value("Bronfman Library"))
               .andExpect(jsonPath("$.host.name").value("Jane Doe")); 
    }

    /**
     * Tests that the controller safely handles requests for events that don't exist.
     * Expects an HTTP 500 Internal Server Error because the controller catches the NullPointerException.
     */
    @Test
    void getEvent_returnsErrorWhenNotFound() throws Exception {
        // Fire GET request for a totally fake ID
        mockMvc.perform(get("/api/events/fake_missing_event")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isInternalServerError());
    }

    /**
     * Tests leaving an event via HTTP POST map payload.
     * Simulates a user actively abandoning a study session.
     */
    @Test
    void leaveEvent_returnsSuccessMessage() throws Exception {
        // 1. Setup Event with the student already inside
        Event e = new Event();
        e.setId("evt_002");
        e.setHost("host_user"); // Needed for the security check in the controller
        e.setAttendees(new ArrayList<>(List.of("student_99")));
        StubDatabase.EVENTS.add(e);

        // 2. Setup Student with the event already inside their attended list
        Student s = new Student();
        s.setUserId("student_99");
        s.setAttendedEventIds(new ArrayList<>(List.of("evt_002")));
        StubDatabase.STUDENTS.add(s);

        // 3. JSON payload to leave
        String jsonPayload = "{\"userId\":\"student_99\", \"eventId\":\"evt_002\"}";

        // 4. Fire POST request WITH Auth Header and correct expected values
        mockMvc.perform(post("/api/events/leave")
                .header("Authorization", "student_99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
               .andExpect(status().isOk()) // <--- Controller returns 200 OK, not 201 Created
               .andExpect(content().string("Successfully removed from event!"));
    }

    /**
     * Tests fetching all events when the database is completely empty.
     * It should gracefully return an empty JSON array `[]` rather than crashing.
     */
    @Test
    void getAllEvents_returnsEmptyListWhenNoEventsExist() throws Exception {
        // We purposely do not add anything to StubDatabase.EVENTS

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray()) // Asserts the root element is a JSON array
               .andExpect(jsonPath("$").isEmpty()); // Asserts the array has 0 elements
    }

    /**
     * Tests Spring Boot's built-in parameter validation.
     * If the frontend forgets to send the "Authorization" header to a DELETE request,
     * Spring should intercept it and return a 400 Bad Request before even hitting the controller logic.
     */
    @Test
    void deleteEvent_returnsBadRequestWhenAuthHeaderMissing() throws Exception {
        // Setup an event
        Event e = new Event();
        e.setId("evt_delete_me");
        e.setHost("host_123");
        StubDatabase.EVENTS.add(e);

        // Fire DELETE request WITHOUT the .header("Authorization", ...)
        mockMvc.perform(delete("/api/events/evt_delete_me"))
               .andExpect(status().isBadRequest());
    }
}
