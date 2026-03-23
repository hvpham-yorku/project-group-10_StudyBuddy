package ca.yorku.my.StudyBuddy.services;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Comment;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Review;
import ca.yorku.my.StudyBuddy.classes.Student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("stub") // <-- Only loads when application.properties says "stub"
public class StubEventRepository implements EventRepository {

    // We inject the interface so this works regardless of which Student DB is active!
    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Event createEvent(Event event) throws Exception {
        // Generate a fake Firestore-style ID
        event.setId("stub_" + UUID.randomUUID().toString().substring(0, 8));
        StubDatabase.EVENTS.add(event);
        return event;
    }

    @Override
    public List<Event> getAllEvents() throws Exception {
        return new ArrayList<>(StubDatabase.EVENTS);
    }

    @Override
    public Event getEventById(String eventId) throws Exception {
        return StubDatabase.EVENTS.stream()
                .filter(e -> e.getId() != null && e.getId().equals(eventId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean deleteEvent(String eventId, String userId) throws Exception {
        Event event = getEventById(eventId);
        if (event == null || !userId.equals(event.getHost())) {
            return false;
        }
        return StubDatabase.EVENTS.removeIf(e -> e.getId().equals(eventId));
    }

    @Override
    public boolean joinEvent(String currentUserId, String eventId) throws Exception {
        Student s = studentRepository.getStudent(currentUserId);
        Event e = getEventById(eventId);

        if (s == null || e == null) {
            return false;
        }

        List<String> newAttendedEventIds = s.getAttendedEventIds();
        if (newAttendedEventIds == null) newAttendedEventIds = new ArrayList<>();

        if (!newAttendedEventIds.contains(e.getId())) {
            newAttendedEventIds.add(e.getId());
            studentRepository.updateAttendedEventIDs(currentUserId, newAttendedEventIds);
        }

        return addAttendee(eventId, currentUserId);
    }

    @Override
    public boolean leaveEvent(String currentUserId, String eventId) throws Exception {
        Student s = studentRepository.getStudent(currentUserId);
        Event e = getEventById(eventId);

        if (s != null && e != null) {
            removeAttendee(eventId, currentUserId);

            List<String> newAttendedEventIds = s.getAttendedEventIds();
            if (newAttendedEventIds != null) {
                newAttendedEventIds.remove(eventId);
                studentRepository.updateAttendedEventIDs(currentUserId, newAttendedEventIds);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean addAttendee(String eventId, String studentId) throws Exception {
        Event e = getEventById(eventId);
        if (e == null) return false;

        List<String> attendees = e.getAttendees();
        if (attendees == null) attendees = new ArrayList<>();

        if (!attendees.contains(studentId)) {
            attendees.add(studentId);
            e.setAttendees(attendees);
        }
        return true;
    }

    @Override
    public boolean removeAttendee(String eventId, String studentId) throws Exception {
        Event e = getEventById(eventId);
        if (e == null) return false;

        List<String> attendees = e.getAttendees();
        if (attendees != null && attendees.contains(studentId)) {
            attendees.remove(studentId);
            e.setAttendees(attendees);
            return true;
        }
        return false;
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