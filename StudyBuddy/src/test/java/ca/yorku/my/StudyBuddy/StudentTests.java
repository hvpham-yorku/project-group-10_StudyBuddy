package ca.yorku.my.StudyBuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class StudentTests {

    @Test
    void emptyConstructor_initializesCollections() {
        Student s = new Student();

        assertNotNull(s.getCourses());
        assertNotNull(s.getStudyVibes());
        assertNotNull(s.getPrivacySettings());
        assertNotNull(s.getNotifications());
        assertNotNull(s.getAttendedEventIds());
    }

    @Test
    void mainConstructor_setsDefaults() {
        Student s = new Student("u1", "John", "Doe");

        assertEquals("u1", s.getUserId());
        assertEquals("John", s.getFirstName());
        assertEquals("Doe", s.getLastName());
        assertEquals("John Doe", s.getFullName());
    }

    @Test
    void setters_updateFields() {
        Student s = new Student();
        s.setEmail("test@yorku.ca");
        s.setBio("Hello");

        assertEquals("test@yorku.ca", s.getEmail());
        assertEquals("Hello", s.getBio());
    }
}
