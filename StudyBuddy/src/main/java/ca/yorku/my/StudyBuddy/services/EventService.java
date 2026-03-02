package ca.yorku.my.StudyBuddy.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;

import org.springframework.stereotype.Service;

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
public class EventService {

    private static final String COLLECTION_NAME = "events";

    public Event createEvent(Event event) throws ExecutionException, InterruptedException {
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

    // Retrieves all events from Firestore.
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

    // Deletes the specified event if the user is the host. Returns true if deletion was successful, false otherwise.
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
    
    // Join event
    public boolean joinEvent(String currentUserId, String eventId) throws Exception, ExecutionException, InterruptedException {
		StudentService ss = new StudentService();
		Student s;
		try {
			s = ss.getStudent(currentUserId);
		} catch (Exception e) {
			return false;
		}
		
    	Event e = getEventById(eventId);
        	
        // Check if user and event exist
    	if (s == null) {
    		throw new Exception("Student does not exist!");
    	} else if (e == null) {
    		throw new Exception("Event does not exist!");
    	}
    	else {
    		// Update student to show that they attended this event
    		List<String> newAttendedEventIds = s.getAttendedEventIds();
    		newAttendedEventIds.add(e.getId());
    		ss.updateAttendedEventIDs(currentUserId, newAttendedEventIds);
    		
    		// Update the event to show that student went to it
    		updateAttendees(eventId, currentUserId);
    		
    		return true;
    	}
    }
    
    public boolean updateAttendees(String eventId, String studentId) throws Exception {
    	
    	StudentService ss = new StudentService();
    	Student s;
    	try {
    		s = ss.getStudent(studentId);
    	} catch (Exception e) {
    		return false;
    	}
    	
    	Event e = this.getEventById(eventId);
    	if (e == null) return false;
    	else {
    		List<String> newAttendees = e.getAttendees();
    		if (newAttendees == null) {
    			newAttendees = new ArrayList<String>();
    		}
    		newAttendees.add(studentId);
    		
    		Firestore db = FirestoreClient.getFirestore();
        	db.collection("events").document(eventId).update("attendees", newAttendees).get();
        	
    		
    		
    	}
    	
    	return true;
    }
    
    // Leave event
    public boolean leaveEvent(String currentUserId, String eventId) throws ExecutionException, InterruptedException {
    	try {
			StudentService ss = new StudentService();
	    	Student s = ss.getStudent(currentUserId);
	    	Event e = getEventById(eventId);
        	
        // Check if user and event exist
    	if (s != null && e!= null) {
    		// Update student attended events
    		List<String> newList = s.getAttendedEventIds();
    		int eventIndex = newList.indexOf(eventId);
    		
    		if (eventIndex != -1) {
    			newList.remove(eventIndex);
    			s.setAttendedEventIds(newList);
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
    
}