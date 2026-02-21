package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// This class is responsible for handling HTTP requests related to events.
// It defines endpoints for creating, retrieving, and deleting events. 
// It also uses EventService to perform the necessary operations on the Firestore database and returns appropriate HTTP responses based on the outcome of each operation.



@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    // This mapping endpoint allows clients to create a new event by sending a POST request with event details in the request body. It returns the created event with its generated ID if successful, or an error status if there was an issue.

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventResponseDTO eventDTO) {
        try {
        	// 1. Create new event; This is where DTO comes in handy so as to filter out any other values
        	Event newEvent = new Event(
        		eventDTO.hostId(),
        		eventDTO.title(),
        		eventDTO.course(),
        		eventDTO.location(),
        		eventDTO.description(),
        		eventDTO.date(),
        		"TBD",
        		eventDTO.maxParticipants()
        	);
        	
        	// 2. Store the event in firebase
        	eventService.createEvent(newEvent);
        		
        	// X. Print it back to user to indicate success
        	return ResponseEntity.status(HttpStatus.CREATED).body(eventDTO);
            //Event createdEvent = eventService.createEvent(event);
            // return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This mapping endpoint allows clients to retrieve a list of all events by sending a GET request. It returns the list of events if successful, or an error status if there was an issue.

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        try {
            // 1. Get the raw Events from firestore
        	List<Event> events = eventService.getAllEvents();

            // 2. Empty list to hold formatted DTOs
            List<EventResponseDTO> eventDTOs = new ArrayList<>();

            // 3. Loop through each event, get the host details, and create a DTO for each event
            for (Event event : events) {
                EventResponseDTO dto = new EventResponseDTO(
                    event.getEventId(), 				// id
                    event.getTitle(),					// title
                    event.getCourse(),			// course
                    event.getLocation(),		// location
                    event.getDescription(), //description
                    //status=event.getStatus(),
                    "Placeholder status",		//status
                    "", // date
                    "", // time
                    0, //duration
                    event.getMaxCapacity(), // maxParticipants
                    event.getParticipantIds(), // attendees
                    List.of("Placeholder tag 1", "Placeholder tag 2"), // tags
                    List.of("Placeholder review 1", "Placeholder review 2"), // reviews
                    event.getHostId(), // hostId
                    "Placeholder name",  // hostName
                    "Placeholder avatar" // hostAvatar
                );
                eventDTOs.add(dto);
            }
                		
            return ResponseEntity.ok(eventDTOs);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This mapping endpoint allows clients to delete a specific event by sending a DELETE request with the event ID in the URL path and the user ID as a request parameter. It returns a no content status if deletion was successful, a forbidden status if the user is not authorized to delete the event, or an error status if there was an issue.

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String eventId,k
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