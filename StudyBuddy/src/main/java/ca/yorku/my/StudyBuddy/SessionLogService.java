package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**  This class manages the session logs of students. It updates the user's session  
*    when an event is created or joined. It includes information such as duration and location
*/
@Service
public class SessionLogService {

    private static final String EVENTS_COLLECTION = "events";


    /**
     * Retrieves all events a student has attended based on their attended event IDs.
     */
    public List<Event> getStudentSessionLog(List<String> attendedEventIds) throws ExecutionException, InterruptedException {
        List<Event> sessionLog = new ArrayList<>();

        if (attendedEventIds == null || attendedEventIds.isEmpty()) {
            return sessionLog;
        }

        Firestore db = FirestoreClient.getFirestore();

        for (String eventId : attendedEventIds) {
            DocumentSnapshot doc = db.collection(EVENTS_COLLECTION).document(eventId).get().get();
            if (doc.exists()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    event.setEventId(doc.getId());
                    sessionLog.add(event);
                }
            }
        }

        return sessionLog;
    }

    /**
     * Adds an event to a student's session log, if they attended the event. 
     * This updates the student's attendedEventIds list.
     */
    public void addEventToSessionLog(Student student, String eventId) {
        student.addAttendedEvent(eventId);
    }

    /**
     * Filters the student's session log by course code 
     */
    public List<Event> filterSessionLogByCourse(List<Event> sessionLog, String courseCode) {
        List<Event> filtered = new ArrayList<>();
        for (Event event : sessionLog) {
            if (event.getCourse() != null && event.getCourse().equals(courseCode)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Calculates total study hours based on a student's session log
     */
    public long getTotalStudyMinutes(List<Event> sessionLog) {
        long totalMinutes = 0;
        for (Event event : sessionLog) {
            if (event.getStartTime() != null && event.getEndTime() != null) {
                try {
                    Instant start = parseToInstant(event.getStartTime());
                    Instant end = parseToInstant(event.getEndTime());
                    totalMinutes += Duration.between(start, end).toMinutes();
                } catch (DateTimeParseException ex) {
                    // skip events with unparseable timestamps
                }
            }
        }
        return totalMinutes;
    }

    private Instant parseToInstant(String ts) {
        // Try ISO instant first 
        try {
            return Instant.parse(ts);
        } catch (DateTimeParseException ignored) {}

        // Try offset datetime 
        try {
            return OffsetDateTime.parse(ts).toInstant();
        } catch (DateTimeParseException ignored) {}

        // Fallback to plain LocalDateTime using system zone
        return LocalDateTime.parse(ts).atZone(ZoneId.systemDefault()).toInstant();
    }
}
