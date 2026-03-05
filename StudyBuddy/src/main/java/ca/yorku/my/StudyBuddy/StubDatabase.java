package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StubDatabase {
    public static final List<Student> STUDENTS = new CopyOnWriteArrayList<>();
    public static final List<Event> EVENTS = new CopyOnWriteArrayList<>();
    public static final List<Chat> CHATS = new CopyOnWriteArrayList<>();
    public static final List<Message> MESSAGES = new CopyOnWriteArrayList<>();
    public static final List<FriendRequest> FRIEND_REQUESTS = new CopyOnWriteArrayList<>();

    static {
        Student s1 = new Student("u1", "Alex", "Johnson");
        s1.setEmail("alex@my.yorku.ca");
        s1.setProgram("Computer Science");
        s1.setYear("3rd Year");
        s1.setIsOnline(true);
        
        Student s2 = new Student("u2", "Sarah", "Chen");
        s2.setEmail("sarah@my.yorku.ca");
        s2.setProgram("Software Engineering");
        s2.setYear("2nd Year");
        s2.setIsOnline(true);

        Student s3 = new Student("u3", "Marcus", "Williams");
        s3.setEmail("marcus@my.yorku.ca");
        s3.setProgram("Mathematics");
        s3.setYear("4th Year");
        s3.setIsOnline(false);

        STUDENTS.addAll(Arrays.asList(s1, s2, s3));

        Event e1 = new Event();
        e1.setId("e1");
        e1.setTitle("EECS 3311 Design Patterns");
        e1.setCourse("EECS 3311");
        e1.setLocation("Scott Library");
        e1.setDate("2026-03-10");
        e1.setTime("14:00");
        e1.setHost("u2");
        e1.setAttendees(new ArrayList<>(Arrays.asList("u2", "u1")));
        e1.setMaxParticipants(5);
        e1.setDescription("Studying factory and singleton patterns.");

        Event e2 = new Event();
        e2.setId("e2");
        e2.setTitle("MATH 2030 Midterm Prep");
        e2.setCourse("MATH 2030");
        e2.setLocation("Bergeron Centre");
        e2.setDate("2026-03-12");
        e2.setTime("10:00");
        e2.setHost("u3");
        e2.setAttendees(new ArrayList<>(Arrays.asList("u3")));
        e2.setMaxParticipants(4);
        e2.setDescription("Going over past midterms.");

        EVENTS.addAll(Arrays.asList(e1, e2));
    }
    
    public static void clearAll() {
        STUDENTS.clear();
        EVENTS.clear();
        CHATS.clear();
        MESSAGES.clear();
        FRIEND_REQUESTS.clear();
    }
}