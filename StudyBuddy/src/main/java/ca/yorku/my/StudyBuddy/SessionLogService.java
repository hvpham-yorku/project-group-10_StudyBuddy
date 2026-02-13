package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**  This class manages the session logs of students. It updates the user's session  
*    when an event is created or joined. It includes information such as duration and location
*/
@Service
public class SessionLogService {

    @Autowired
    private EventService eventService;

    private static final String EVENTS_COLLECTION = "events";

    /**
     * Retrieves all events a student has attended based on their attended event IDs.
     *
     * @param attendedEventIds List of event IDs the student has attended
     * @return List of Event objects the student attended
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
     * Adds an event to a student's session log (marks student as attended).
     * This updates the student's attendedEventIds list.
     *
     * @param student The Student object
     * @param eventId The ID of the event to add to the session log
     */
    public void addEventToSessionLog(Student student, String eventId) {
        student.addAttendedEvent(eventId);
    }

    /**
     * Filters the student's session log by course code.
     *
     * @param sessionLog List of events in the student's session log
     * @param courseCode The course code to filter by
     * @return Filtered list of events
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
     * Calculates total study hours for a student's session log.
     *
     * @param sessionLog List of events in the student's session log
     * @return Total minutes studied
     */
    public long getTotalStudyMinutes(List<Event> sessionLog) {
        long totalMinutes = 0;
        for (Event event : sessionLog) {
            if (event.getStartTime() != null && event.getEndTime() != null) {
                totalMinutes = Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
            }
        }
        return totalMinutes;
    }
}
