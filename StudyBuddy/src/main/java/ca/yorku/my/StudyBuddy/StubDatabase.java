package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory data holder used by the "stub" profile.
 */
public class StubDatabase {
    public static final List<Student> STUDENTS = new CopyOnWriteArrayList<>();
    public static final List<Event> EVENTS = new CopyOnWriteArrayList<>();
    public static final List<Chat> CHATS = new CopyOnWriteArrayList<>();
    public static final List<Message> MESSAGES = new CopyOnWriteArrayList<>();
    public static final List<FriendRequest> FRIEND_REQUESTS = new CopyOnWriteArrayList<>();

    static {
        // Initial Seed Data
        Student s = new Student("123", "John", "Doe");
        s.setEmail("sean@yorku.ca");
        s.setIsOnline(true);
        s.setLocation("Steacie");
        s.setJoinedDate("2024-01-01");
        STUDENTS.add(s);
    }
    
    // Helper method to easily wipe the database during automated tests
    public static void clearAll() {
        STUDENTS.clear();
        EVENTS.clear();
        CHATS.clear();
        MESSAGES.clear();
        FRIEND_REQUESTS.clear();
    }
}