package ca.yorku.my.StudyBuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyBuddyApplication {

	public static void main(String[] args) {
		
		// TEMPORARY CODE FOR REFERENCE
    	String[] s1Courses = {"LE-EECS-2311-Z", "SC-PHYS-2020-M", "LE-EECS-4413-Z", "LE-EECS-3421-Z", "LE-EECS-2101-X"};
    	StubDatabase.STUDENTS.add(new Student("John", "Doe", s1Courses));
    	
    	
		SpringApplication.run(StudyBuddyApplication.class, args);
	}

}
