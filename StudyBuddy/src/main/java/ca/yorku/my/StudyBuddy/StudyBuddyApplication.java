package ca.yorku.my.StudyBuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyBuddyApplication {

    public static void main(String[] args) {
        //  removed the hardcoded test student because the Student model has changed.
        //  will now use  'curl' command to add students dynamically
        SpringApplication.run(StudyBuddyApplication.class, args);
    }
}