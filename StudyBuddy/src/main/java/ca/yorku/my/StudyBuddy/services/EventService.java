package ca.yorku.my.StudyBuddy.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Event;

import org.springframework.stereotype.Service;

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
}