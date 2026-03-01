package ca.yorku.my.StudyBuddy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void studentConstructor_setsFieldsCorrectly() {
        Student s = new Student("123", "John", "Doe");

        assertThat(s.getUserId()).isEqualTo("123");
        assertThat(s.getFirstName()).isEqualTo("John");
        assertThat(s.getLastName()).isEqualTo("Doe");
    }

    @Test
    void setters_updateFieldsCorrectly() {
        Student s = new Student("123", "John", "Doe");

        s.setProgram("CS");
        s.setYear("3");
        s.setCourses(List.of("EECS 2311"));

        assertThat(s.getProgram()).isEqualTo("CS");
        assertThat(s.getYear()).isEqualTo("3");
        assertThat(s.getCourses()).containsExactly("EECS 2311");
    }

    @Test
    void onlineStatus_updatesCorrectly() {
        Student s = new Student("123", "John", "Doe");

        s.setIsOnline(true);

        assertThat(s.getIsOnline()).isTrue();
    }

    
}
