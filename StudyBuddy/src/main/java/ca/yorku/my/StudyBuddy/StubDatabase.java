package ca.yorku.my.StudyBuddy;
import ca.yorku.my.StudyBuddy.classes.Student;

import java.util.ArrayList;

/**
 * Simple in-memory data holder used by student-related development endpoints/tests.
 */
public class StubDatabase {
    public static ArrayList<Student> STUDENTS = new ArrayList<>();

    static {
        Student s = new Student("123", "John", "Doe");

		s.setEmail("sean@yorku.ca");
		s.setIsOnline(true);
		s.setLocation("Steacie");
		s.setJoinedDate("2024-01-01");
    
	    STUDENTS.add(s);

	}
}