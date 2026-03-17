package ca.yorku.my.StudyBuddy.controllers;
import ca.yorku.my.StudyBuddy.services.EventRepository;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.EventResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.HostDTO;
import ca.yorku.my.StudyBuddy.services.AuthService;
import ca.yorku.my.StudyBuddy.services.EventService;
import ca.yorku.my.StudyBuddy.services.StudentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// This class is responsible for handling HTTP requests related to events.
// It defines endpoints for creating, retrieving, and deleting events. 
// It also uses EventService to perform the necessary operations on the Firestore database and returns appropriate HTTP responses based on the outcome of each operation.



@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventRepository eventService;
    
    @Autowired
    private AuthRepository authService;
    
    @Autowired
    private StudentRepository studentService;

    // This mapping endpoint allows clients to create a new event by sending a POST request with event details in the request body. It returns the created event with its generated ID if successful, or an error status if there was an issue.
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestHeader("Authorization") String authHeader, @RequestBody EventResponseDTO eventDTO) {
        try {
        	
        	// 1. Create new event; This is where DTO comes in handy so as to filter out any other values
        	
        	String hostId = authService.verifyFrontendToken(authHeader);
        	
        	Event newEvent = new Event(
        		eventDTO.id(),
        		eventDTO.title(),
        		eventDTO.course(),
        		hostId,
        		eventDTO.location(),
        		eventDTO.date(),
        		eventDTO.time(),
        		eventDTO.duration(),
        		eventDTO.description(),
        		eventDTO.maxParticipants(),
        		eventDTO.attendees(),
        		eventDTO.tags(),
        		eventDTO.status(),
        		eventDTO.reviews()
        	);
        	
        	// 2. Store the event in firebase
        	eventService.createEvent(newEvent);
        		
        	// 3. Print it back to user to indicate success
        	return ResponseEntity.status(HttpStatus.CREATED).body(eventDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // TODO: Make DTO
    @PostMapping("/join")
    public ResponseEntity<String> joinEvent(@RequestBody Map<String, String> payload) {
        try {
        	// 1. Initiate the service
    		eventService.joinEvent(payload.get("userId"), payload.get("eventId"));
        	
        	// 2. Print it back to user to indicate success
        	return ResponseEntity.status(HttpStatus.CREATED).body("Joined Event!");
        } catch (Exception e) {
        	e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
 // TODO: Make DTO
    @PostMapping("/leave")
    public ResponseEntity<String> leaveEvent(@RequestBody Map<String, String> payload) {
        try {
        	// 1. Initiate the service
    		eventService.leaveEvent(payload.get("userId"), payload.get("eventId"));
        	
        	// 2. Print it back to user to indicate success
        	return ResponseEntity.status(HttpStatus.CREATED).body("Left Event!");
        } catch (Exception e) {
        	e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEvent(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
    	try {
    		Event event = eventService.getEventById(eventId);
    		
    		Student hostStudent = studentService.getStudent(event.getHost());
            HostDTO hostDTO = new HostDTO(
                hostStudent.getUserId(),
                hostStudent.getFullName(),
                hostStudent.getAvatar()
            );

            // 1. Identify the user making the request
            String requesterId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    requesterId = authService.verifyFrontendToken(authHeader);
                } catch (Exception e) {
                    // Invalid token, treat as anonymous
                }
            }

            // 2. Safely get the raw attendees list and count
            List<String> rawAttendees = event.getAttendees() != null ? event.getAttendees() : new ArrayList<>();
            int attendeeCount = rawAttendees.size();
            
            // 3. Check if the requester has permission to see the attendees
            boolean isParticipating = requesterId != null && 
                (requesterId.equals(event.getHost()) || rawAttendees.contains(requesterId));

            // 4. Filter the list: send the real list if participating, otherwise send empty array
            List<String> visibleAttendees = isParticipating ? rawAttendees : new ArrayList<>();
    		
    		EventResponseDTO dto = new EventResponseDTO(
                	event.getId(),
                	event.getTitle(),
                	event.getCourse(),
                	hostDTO,
                	event.getLocation(),
                	event.getDate(),
                	event.getTime(),
                	event.getDuration(),
                	event.getDescription(),
                	event.getMaxParticipants(),
                    attendeeCount,
                    visibleAttendees,
                	event.getTags() != null ? event.getTags() : new ArrayList<>(),
                	event.getStatus() != null ? event.getStatus() : "upcoming",
                	event.getReviews() != null ? event.getReviews() : new ArrayList<>()
                );
    		
    		return ResponseEntity.ok(dto);
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    	}
    }

    // This mapping endpoint allows clients to delete a specific event by sending a DELETE request with the event ID in the URL path and the user ID as a request parameter. 
    // It returns a no content status if deletion was successful, a forbidden status if the user is not authorized to delete the event, or an error status if there was an issue.
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<Event> events = eventService.getAllEvents();
            List<EventResponseDTO> eventDTOs = new ArrayList<>();

            // 1. Identify the user making the request
            String requesterId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    requesterId = authService.verifyFrontendToken(authHeader);
                } catch (Exception e) {
                    // Invalid token, treat as anonymous
                }
            }

            for (Event event : events) {
                Student hostStudent = studentService.getStudent(event.getHost());
                HostDTO hostDTO = new HostDTO(
                    hostStudent.getUserId(),
                    hostStudent.getFullName(),
                    hostStudent.getAvatar()
                );

                List<String> rawAttendees = event.getAttendees() != null ? event.getAttendees() : new ArrayList<>();
                int attendeeCount = rawAttendees.size();
                
                boolean isParticipating = requesterId != null && 
                    (requesterId.equals(event.getHost()) || rawAttendees.contains(requesterId));

                List<String> visibleAttendees = isParticipating ? rawAttendees : new ArrayList<>();

                EventResponseDTO dto = new EventResponseDTO(
                	event.getId(), event.getTitle(), event.getCourse(),
                	hostDTO,
                	event.getLocation(), event.getDate(), event.getTime(),
                	event.getDuration(), event.getDescription(),
                	event.getMaxParticipants(), 
                    attendeeCount,
                    visibleAttendees,
                	event.getTags() != null ? event.getTags() : new ArrayList<>(), 
                    event.getStatus() != null ? event.getStatus() : "upcoming", 
                    event.getReviews() != null ? event.getReviews() : new ArrayList<>()
                );
                eventDTOs.add(dto);
            }
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authHeader) {
        try {
        	String userId = authService.verifyFrontendToken(authHeader);
            System.out.println("Attempted to delete " + eventId + " as " + userId);
            
            boolean deleted = eventService.deleteEvent(eventId, userId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
            	System.out.println("Failed Deletion");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}