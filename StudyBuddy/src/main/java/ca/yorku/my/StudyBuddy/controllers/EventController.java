package ca.yorku.my.StudyBuddy.controllers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import ca.yorku.my.StudyBuddy.classes.Comment;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.dtos.JoinEventRequest;
import ca.yorku.my.StudyBuddy.dtos.LeaveEventRequest;
import ca.yorku.my.StudyBuddy.dtos.AddReviewRequest;
import ca.yorku.my.StudyBuddy.dtos.AddCommentRequest;
import ca.yorku.my.StudyBuddy.classes.Review;
import ca.yorku.my.StudyBuddy.dtos.EventResponseDTO;
import ca.yorku.my.StudyBuddy.mappers.EventMapper;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.EventRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

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

    @Autowired
    private EventMapper eventMapper;

    /**
     * Helper method to extract the requesterId from the authorization header.
     * Returns null if the header is missing or invalid.
     */
    private String extractRequesterId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return authService.verifyFrontendToken(authHeader);
            } catch (Exception e) {
                // Invalid token, treat as anonymous
            }
        }
        return null;
    }

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
    
    @PostMapping("/join")
    public ResponseEntity<String> joinEvent(
            @RequestHeader("Authorization") String authHeader, 
            @Valid @RequestBody JoinEventRequest request) {
        try {
            // 1. Verify the token to see who is ACTUALLY making the request
            String requesterId = authService.verifyFrontendToken(authHeader);
            
            // 2. Ignore any userId in the payload. Force the requester to only join for themselves.
            boolean success = eventService.joinEvent(requesterId, request.eventId());
            
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Joined Event!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to join event");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/leave")
    public ResponseEntity<String> leaveEvent(
            @RequestHeader("Authorization") String authHeader, 
            @Valid @RequestBody LeaveEventRequest request) {
        try {
            // 1. Verify who is making the request
            String requesterId = authService.verifyFrontendToken(authHeader);
            
            // 2. Extract who they are trying to remove
            String targetUserId = request.userId();
            String eventId = request.eventId();
            
            // 3. Verify the user
            if (!requesterId.equals(targetUserId)) {
                ca.yorku.my.StudyBuddy.classes.Event event = eventService.getEventById(eventId);
                
                if (event == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
                }
                
                // If they aren't removing themselves and they aren't the host, block request
                if (!event.getHost().equals(requesterId)) {
                    System.out.println("SECURITY ALERT: User " + requesterId + " attempted to kick " + targetUserId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the host can kick users.");
                }
            }

            // 4. If they pass the security check, execute the removal
            boolean success = eventService.leaveEvent(targetUserId, eventId);
            
            if (success) {
                return ResponseEntity.status(HttpStatus.OK).body("Successfully removed from event!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to remove from event");
            }
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
    		
            String requesterId = extractRequesterId(authHeader);

            // --- THE MAGIC HAPPENS HERE ---
            EventResponseDTO dto = eventMapper.toResponseDTO(event, requesterId);
    		
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

            String requesterId = extractRequesterId(authHeader);

            for (Event event : events) {
                eventDTOs.add(eventMapper.toResponseDTO(event, requesterId));
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
    
    @PostMapping("/{eventId}/reviews")
    public ResponseEntity<?> addReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String eventId,
            @Valid @RequestBody AddReviewRequest request) {

        try {
            String userId = authService.verifyFrontendToken(authHeader);
            
            Review review = new Review(
                "r_" + System.currentTimeMillis(),
                userId,
                request.rating(),
                request.text().trim(),
                java.time.LocalDate.now().toString(),
                new ArrayList<>()
            );

            boolean success = eventService.addReview(eventId, review);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(review);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/{eventId}/reviews/{reviewId}/comments")
    public ResponseEntity<?> addReviewComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String eventId,
            @PathVariable String reviewId,
            @Valid @RequestBody AddCommentRequest request) {

        try {
            String userId = authService.verifyFrontendToken(authHeader);

            Comment comment = new Comment(
                "c_" + System.currentTimeMillis(),
                userId,
                request.text().trim(),
                java.time.LocalDate.now().toString()
            );

            boolean success = eventService.addCommentToReview(eventId, reviewId, comment);
            
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(comment);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event or Review not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve all reviews for a specific event.
     * Provides cleaner API separation than embedding reviews in event response.
     */
    @GetMapping("/{eventId}/reviews")
    public ResponseEntity<?> getEventReviews(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Event event = eventService.getEventById(eventId);
            
            if (event == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
            }
            
            // Return the reviews directly - frontend can now query reviews separately
            List<Review> reviews = event.getReviews();
            if (reviews == null) {
                reviews = new ArrayList<>();
            }
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error: " + e.getMessage());
        }
    }
    
}
