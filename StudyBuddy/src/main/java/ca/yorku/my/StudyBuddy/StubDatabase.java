package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;

public class StubDatabase {
    public static final ArrayList<Student> STUDENTS = new ArrayList<>();

    static {
        Student s = new Student("123", "John", "Doe");

		s.setEmail("sean@yorku.ca");
		s.setIsOnline(true);
		s.setLocation("Steacie");
		s.setJoinedDate("2024-01-01");
    
	    STUDENTS.add(s);

	}
}