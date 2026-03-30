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
import org.springframework.beans.factory.annotation.Autowired;

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
    private final StudentRepository studentRepository;

    @Autowired
    public EventService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

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
        Student student = studentRepository.getStudent(currentUserId);
        Event event = getEventById(eventId);

        if (student == null) {
            throw new IllegalArgumentException("Student does not exist");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event does not exist");
        }

        List<String> attended = student.getAttendedEventIds() == null
            ? new ArrayList<>()
            : new ArrayList<>(student.getAttendedEventIds());

        if (!attended.contains(eventId)) {
            attended.add(eventId);
            studentRepository.updateAttendedEventIDs(currentUserId, attended);
        }

        addAttendee(eventId, currentUserId);
        return true;
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
            Student student = studentRepository.getStudent(currentUserId);
            Event event = getEventById(eventId);

            // Check if user and event exist
            if (student != null && event != null) {
                // Update event attendees
                this.removeAttendee(eventId, currentUserId);

                // Update student's attended events
                List<String> attended = student.getAttendedEventIds();

                if (student.getAttendedEventIds() == null) {
                    studentRepository.updateAttendedEventIDs(currentUserId, null);
                } else {
                    int removeIndex = attended.indexOf(eventId);
                    if (removeIndex != -1) {
                        attended.remove(removeIndex);
                        studentRepository.updateAttendedEventIDs(currentUserId, attended);
                    }
                }
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
        try {
            studentRepository.getStudent(studentId);
        } catch (Exception e) {
            return false;
        }

        Event event = this.getEventById(eventId);
        if (event == null) return false;

        List<String> attendees = event.getAttendees();
        if (attendees == null) {
            attendees = new ArrayList<>();
        }

        // Prevent duplicates in event document
        if (!attendees.contains(studentId)) {
            attendees.add(studentId);
            Firestore db = FirestoreClient.getFirestore();
            db.collection("events").document(eventId).update("attendees", attendees).get();
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
        try {
            studentRepository.getStudent(studentId);
        } catch (Exception e) {
            return false;
        }

        Event event = this.getEventById(eventId);
        if (event == null) return false;

        List<String> attendees = event.getAttendees();
        if (attendees == null) {
            attendees = new ArrayList<>();
        }

        int removeIndex = attendees.indexOf(studentId);
        if (removeIndex == -1) return false;
        attendees.remove(removeIndex);

        Firestore db = FirestoreClient.getFirestore();
        db.collection("events").document(eventId).update("attendees", attendees).get();
        return true;
    }
    
    /**
     * Add a review to an event in Firestore
     */
    @Override
    public boolean addReview(String eventId, ca.yorku.my.StudyBuddy.classes.Review review) throws Exception {
        Event e = this.getEventById(eventId);
        if (e == null) return false;
        
        List<ca.yorku.my.StudyBuddy.classes.Review> reviews = e.getReviews();
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        
        // Append the new review
        reviews.add(review);
        
        // Push the updated array back to Firestore
        Firestore db = FirestoreClient.getFirestore();
        db.collection("events").document(eventId).update("reviews", reviews).get();
        return true;
    }

    /**
     * Add a comment to a specific review inside an event in Firestore
     */
    @Override
    public boolean addCommentToReview(String eventId, String reviewId, ca.yorku.my.StudyBuddy.classes.Comment comment) throws Exception {
        Event e = this.getEventById(eventId);
        if (e == null) return false;
        
        List<ca.yorku.my.StudyBuddy.classes.Review> reviews = e.getReviews();
        if (reviews == null) return false;

        boolean reviewFound = false;
        
        // Find the matching review and inject the comment
        for (ca.yorku.my.StudyBuddy.classes.Review r : reviews) {
            if (r.getId() != null && r.getId().equals(reviewId)) {
                List<ca.yorku.my.StudyBuddy.classes.Comment> comments = r.getComments();
                if (comments == null) {
                    comments = new ArrayList<>();
                }
                comments.add(comment);
                r.setComments(comments);
                reviewFound = true;
                break;
            }
        }
        
        if (!reviewFound) return false;

        // Push the entirely updated reviews array (with the nested comment) back to Firestore
        Firestore db = FirestoreClient.getFirestore();
        db.collection("events").document(eventId).update("reviews", reviews).get();
        return true;
    }
    
}
