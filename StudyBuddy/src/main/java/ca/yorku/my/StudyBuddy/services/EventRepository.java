package ca.yorku.my.StudyBuddy.services;

import ca.yorku.my.StudyBuddy.classes.Comment;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Review;

import java.util.List;

public interface EventRepository {
    Event createEvent(Event event) throws Exception;
    List<Event> getAllEvents() throws Exception;
    Event getEventById(String eventId) throws Exception;
    boolean deleteEvent(String eventId, String userId) throws Exception;
    boolean joinEvent(String currentUserId, String eventId) throws Exception;
    boolean leaveEvent(String currentUserId, String eventId) throws Exception;
    boolean addAttendee(String eventId, String studentId) throws Exception;
    boolean removeAttendee(String eventId, String studentId) throws Exception;
    
    boolean addReview(String eventId, Review review) throws Exception;
    boolean addCommentToReview(String eventId, String reviewId, Comment comment) throws Exception;
}