package ca.yorku.my.StudyBuddy.services;
import ca.yorku.my.StudyBuddy.services.EventRepository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Comment;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Review;
import ca.yorku.my.StudyBuddy.classes.Student;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class is responsible interacting with the Firestore database to perform CRUD operations on event data.
 * 
 */

@Service
@Profile("firestore")
@DependsOn("firebaseConfig")
public class EventService implements EventRepository  {

    private static final String COLLECTION_NAME = "events";

    /**
     * Creates an event 
     * @param event						The event object to translate and store into Firestore
     * @return 							The event that was retrieved from Firestore (or null)
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Event createEvent(Event event) throws Exception{
        Firestore db = FirestoreClient.getFirestore();

        // Adding event to Firestore (auto-generates document ID)
        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(event);
        DocumentReference docRef = future.get();

        // Stores the generated ID back into the event object
        event.setId(docRef.getId());

        // Updates the stored document so it contains eventId too
        docRef.set(event).get();

        return event;
    }

    /**
     * Obtains all events from the Firestore.
     * @return							List of type Events; all the events in Firestore
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public List<Event> getAllEvents() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Event> events = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Event event = doc.toObject(Event.class);
            event.setId(doc.getId());
            events.add(event);
        }

        return events;
    }
    
    /**
     * Returns an event object by given Event ID
     * @param eventId
     * @return						A singular event (or null)
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Event getEventById(String eventId) throws ExecutionException, InterruptedException {
    	Firestore db = FirestoreClient.getFirestore();
    	
    	DocumentReference docRef = db.collection("events").document(eventId);
    	DocumentSnapshot doc = docRef.get().get();
    	
    	if (doc.exists()) {
            Event newEvent = doc.toObject(Event.class);
            newEvent.setId(eventId);
            return newEvent;
    	}
           return null;
    }

    /**
     * Delete event (aka a document) in Firestore
     * @param eventId
     * @param userId
     * @return							Whether or not the deletion was successful
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean deleteEvent(String eventId, String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        DocumentReference docRef = db.collection(COLLECTION_NAME).document(eventId);
        DocumentSnapshot doc = docRef.get().get();

        if (!doc.exists()) {
            return false;
        }

        Event event = doc.toObject(Event.class);
        if (event == null) {
        	return false;
        } else if (!event.getHost().equals(userId)){  // TODO: People are able to forge payload -- should verify with JWT
        	return false;
        } else {
        	docRef.delete().get();
        	return true;
        }
    }
    
    /**
     * Join event and update both student and event document in Firestore
     * @param currentUserId			The user joining the event
     * @param eventId				The event the user is joining
     * @return						If this method works
     * @throws Exception
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean joinEvent(String currentUserId, String eventId) throws Exception, ExecutionException, InterruptedException {
        StudentService ss = new StudentService();
        Student s;
        try {
            s = ss.getStudent(currentUserId);
        } catch (Exception e) {
            return false;
        }
        
        Event e = getEventById(eventId);
            
        if (s == null) {
            throw new Exception("Student does not exist!");
        } else if (e == null) {
            throw new Exception("Event does not exist!");
        } else {
            List<String> newAttendedEventIds = s.getAttendedEventIds();
            if (newAttendedEventIds == null) newAttendedEventIds = new ArrayList<>();
            
            // Prevent duplicates in student document
            if (!newAttendedEventIds.contains(e.getId())) {
                newAttendedEventIds.add(e.getId());
                ss.updateAttendedEventIDs(currentUserId, newAttendedEventIds);
            }
            
            addAttendee(eventId, currentUserId);
            return true;
        }
    }
    
    /**
     * Leave event and update bboth the student and the event documents in Firestore
     * @param currentUserId				The user leaving the event
     * @param eventId					The event the user is leaving from
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public boolean leaveEvent(String currentUserId, String eventId) throws ExecutionException, InterruptedException {
    	try {
			StudentService ss = new StudentService();
	    	Student s = ss.getStudent(currentUserId);
	    	Event e = getEventById(eventId);
        	
        // Check if user and event exist
    	if (s != null && e!= null) {
    		// Update event attendees
    		this.removeAttendee(eventId, currentUserId);
    		
    		// Update student's attended events
    		List<String> newAttendedEventIds = s.getAttendedEventIds();
    		
    		// TODO: This logic should go in StudentService instead of Event. Fix it!
    		// Edge case: Can't leave an event if you didn't join any event
    		if (s.getAttendedEventIds() == null) {
    			ss.updateAttendedEventIDs(currentUserId, null);
    		}
    		
    		// Edge case: Can't leave an event you didn't even join
    		int removeIndex = newAttendedEventIds.indexOf(eventId);
    		if (removeIndex == -1) {
    			return false;
    		} else { // Otherwise, it exists and we should remove it;
    			newAttendedEventIds.remove(removeIndex);
    			ss.updateAttendedEventIDs(currentUserId, newAttendedEventIds);
    		}
    		
    		// Update event students
    		List<String> newAttendees = e.getAttendees();
    		int attendeeIndex = newAttendees.indexOf(eventId);
    		
    		if (attendeeIndex != -1) {
    			newAttendees.remove(attendeeIndex);
    			e.setAttendees(newAttendees);
    		}
    		
    		e.setAttendees(newAttendees);
    	}
    	} catch (Exception error) {
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Add an attendee to the event given by an ID and save to Firestore
     * @param eventId
     * @param studentId
     * @return
     * @throws Exception
     */
    @Override
    public boolean addAttendee(String eventId, String studentId) throws Exception {
        StudentService ss = new StudentService();
        try {
            ss.getStudent(studentId);
        } catch (Exception e) {
            return false;
        }
        
        Event e = this.getEventById(eventId);
        if (e == null) return false;
        else {
            List<String> newAttendees = e.getAttendees();
            if (newAttendees == null) {
                newAttendees = new ArrayList<>();
            }
            
            // Prevent duplicates in event document
            if (!newAttendees.contains(studentId)) {
                newAttendees.add(studentId);
                Firestore db = FirestoreClient.getFirestore();
                db.collection("events").document(eventId).update("attendees", newAttendees).get();
            }
        }
        return true;
    }
    
    /**
     * Remove an attendee of an event by an ID and save the resultant list to Firestore
     * @param eventId
     * @param studentId
     * @return
     * @throws Exception
     */
    @Override
    public boolean removeAttendee(String eventId, String studentId) throws Exception {
    	
    	StudentService ss = new StudentService();
    	Student s;
    	try {
    		s = ss.getStudent(studentId);
    	} catch (Exception e) {
    		return false;
    	}
    	
    	Event e = this.getEventById(eventId);
    	List<String> deductedAttendees;
    	if (e == null) return false;
    	else {
    		deductedAttendees = e.getAttendees();
    		if (deductedAttendees == null) {
    			deductedAttendees = new ArrayList<String>();
    		}
    		
    		int removeIndex = deductedAttendees.indexOf(studentId);
    		if (removeIndex == -1) return false;
    		deductedAttendees.remove(removeIndex);
    	}
    	Firestore db = FirestoreClient.getFirestore();
    	db.collection("events").document(eventId).update("attendees", deductedAttendees).get();
    	return true;
    }
    
    @Override
    public boolean addReview(String eventId, Review review) throws Exception {
        Event e = getEventById(eventId);
        if (e == null) return false;

        List<ca.yorku.my.StudyBuddy.classes.Review> reviews = e.getReviews();
        if (reviews == null) {
            reviews = new ArrayList<>();
            e.setReviews(reviews);
        }

        reviews.add(review);
        return true;
    }

    @Override
    public boolean addCommentToReview(String eventId, String reviewId, Comment comment) throws Exception {
        Event e = getEventById(eventId);
        if (e == null) return false;

        List<ca.yorku.my.StudyBuddy.classes.Review> reviews = e.getReviews();
        if (reviews == null) return false;

        for (ca.yorku.my.StudyBuddy.classes.Review r : reviews) {
            if (r.getId() != null && r.getId().equals(reviewId)) {
                List<Comment> comments = r.getComments();
                if (comments == null) {
                    comments = new ArrayList<>();
                    r.setComments(comments);
                }
                comments.add(comment);
                return true; // Successfully added comment
            }
        }
        
        return false; // Review not found
    }
    
}