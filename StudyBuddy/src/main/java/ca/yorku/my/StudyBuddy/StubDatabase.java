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
        // Add these three lines:
        e1.setTags(new ArrayList<>(Arrays.asList("Group Discussion", "Whiteboard Work")));
        e1.setStatus("upcoming");
        e1.setReviews(new ArrayList<>());

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
        // Add these three lines:
        e2.setTags(new ArrayList<>(Arrays.asList("Problem Solving", "Quiet Focus")));
        e2.setStatus("upcoming");
        e2.setReviews(new ArrayList<>());
        
        Event e3 = new Event();
        e3.setId("e3");
        e3.setTitle("EECS 3000 Ethics Essay Workshop");
        e3.setCourse("EECS 3000");
        e3.setLocation("Bennett Centre");
        e3.setDate("2026-02-23"); // Note the older date!
        e3.setTime("16:30");
        e3.setHost("u3");
        e3.setAttendees(new ArrayList<>(Arrays.asList("u3")));
        e3.setMaxParticipants(10);
        e3.setDescription("Peer review and feedback session for the upcoming ethics essay. Bring a printed draft if possible.");
        e3.setTags(new ArrayList<>(Arrays.asList("Group Discussion")));
        e3.setStatus("past"); // Marked as past!
        e3.setReviews(new ArrayList<>());

        Event e4 = new Event();
        e4.setId("e4");
        e4.setTitle("EECS 2311 Lab 4 Help Session");
        e4.setCourse("EECS 2311");
        e4.setLocation("Lassonde Building");
        e4.setDate("2026-02-19");
        e4.setTime("13:00");
        e4.setHost("u2");
        e4.setAttendees(new ArrayList<>(Arrays.asList("u2", "u1")));
        e4.setMaxParticipants(5);
        e4.setDescription("Debugging and code review for Lab 4. Intermediate Java knowledge expected.");
        e4.setTags(new ArrayList<>(Arrays.asList("Problem Solving", "Whiteboard Work")));
        e4.setStatus("past");
        e4.setReviews(new ArrayList<>()); // You can add mock reviews here later if you want to test the review system!

        Event e5 = new Event();
        e5.setId("e5");
        e5.setTitle("EECS 4080 Final Project Brainstorm");
        e5.setCourse("EECS 4080");
        e5.setLocation("Scott Library");
        e5.setDate("2026-02-25");
        e5.setTime("15:00");
        e5.setHost("u2"); // Sarah is hosting this one
        e5.setAttendees(new ArrayList<>(Arrays.asList("u2")));
        e5.setMaxParticipants(6);
        e5.setDescription("Brainstorming and planning session for the EECS 4080 final project. All ideas welcome!");
        e5.setTags(new ArrayList<>(Arrays.asList("Group Discussion", "Whiteboard Work")));
        e5.setStatus("past");
        e5.setReviews(new ArrayList<>());

        EVENTS.addAll(Arrays.asList(e1, e2, e3, e4, e5));
    }
    
    public static void clearAll() {
        STUDENTS.clear();
        EVENTS.clear();
        CHATS.clear();
        MESSAGES.clear();
        FRIEND_REQUESTS.clear();
    }
}