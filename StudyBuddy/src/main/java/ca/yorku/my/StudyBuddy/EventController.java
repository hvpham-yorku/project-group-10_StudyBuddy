package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
/**
 * Handles event create, read, update, delete endpoints and maps service outcomes to HTTP responses.
 */
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Creates a new event document and returns the stored representation.
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        try {
            Event createdEvent = eventService.createEvent(event);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns all events currently contained in storage.
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes an event when the requesting user is authorized as owner/host.
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String eventId,
            @RequestParam String userId) {
        try {
            boolean deleted = eventService.deleteEvent(eventId, userId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}