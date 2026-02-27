package ca.yorku.my.StudyBuddy.integration;

import ca.yorku.my.StudyBuddy.Event;
import ca.yorku.my.StudyBuddy.EventService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventServiceFirestoreIntegrationTest {

    @Autowired
    private EventService eventService;

    @Test
    void createReadDeleteEventAgainstActualFirestore() throws Exception {
        Path firebaseKey = Path.of("src/main/resources/serviceAccountKey.json");
        Assumptions.assumeTrue(Files.exists(firebaseKey),
                "Skipping Firestore integration test because serviceAccountKey.json is missing");

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String hostId = "integration-host-" + uniqueSuffix;

        Event event = new Event(
                hostId,
                "Integration Test Event " + uniqueSuffix,
                "EECS 2311",
                "Scott Library",
                "Integration test event",
                "2026-03-01T10:00:00Z",
                "2026-03-01T12:00:00Z",
                5
        );

        Event created = eventService.createEvent(event);
        assertNotNull(created.getEventId());

        List<Event> allEvents = eventService.getAllEvents();
        boolean found = allEvents.stream().anyMatch(existing -> created.getEventId().equals(existing.getEventId()));
        assertTrue(found, "Expected created event to be retrievable from Firestore");

        boolean deleted = eventService.deleteEvent(created.getEventId(), hostId);
        assertTrue(deleted, "Expected host to be able to delete integration test event");
    }
}
