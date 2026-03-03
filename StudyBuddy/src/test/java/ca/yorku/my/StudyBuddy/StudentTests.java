package ca.yorku.my.StudyBuddy;
import ca.yorku.my.StudyBuddy.classes.Student;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class StudentTests {

    // -----------------------------
    // Constructors
    // -----------------------------

    @Test
    void emptyConstructor_initializesCollections() {
        Student s = new Student();

        assertNotNull(s.getCourses());
        assertNotNull(s.getStudyVibes());
        assertNotNull(s.getPrivacySettings());
        assertNotNull(s.getNotifications());
        assertNotNull(s.getAttendedEventIds());

        assertThat(s.getCourses()).isEmpty();
        assertThat(s.getAttendedEventIds()).isEmpty();
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
    void fullName_handlesMissingLastName() {
        Student s = new Student("u1", "John", null);
        assertEquals("John", s.getFullName());
    }

    @Test
    void fullName_handlesMissingFirstName() {
        Student s = new Student("u1", null, "Doe");
        assertEquals("Doe", s.getFullName());
    }

    @Test
    void fullName_handlesBothNamesMissing() {
        Student s = new Student("u1", null, null);
        assertEquals("", s.getFullName());
    }

    // -----------------------------
    // Basic setters/getters
    // -----------------------------

    @Test
    void setters_updateFields() {
        Student s = new Student();
        s.setEmail("test@yorku.ca");
        s.setBio("Hello");

        assertEquals("test@yorku.ca", s.getEmail());
        assertEquals("Hello", s.getBio());
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
    void setters_allowNullValues() {
        Student s = new Student("123", "John", "Doe");

        s.setBio(null);
        s.setProgram(null);

        assertNull(s.getBio());
        assertNull(s.getProgram());
    }

    // -----------------------------
    // Boolean flags
    // -----------------------------

    @Test
    void onlineStatus_updatesCorrectly() {
        Student s = new Student("123", "John", "Doe");

        s.setIsOnline(true);
        assertTrue(s.getIsOnline());

        s.setIsOnline(false);
        assertFalse(s.getIsOnline());
    }

    // -----------------------------
    // Collections: mutation behavior
    // -----------------------------

    @Test
    void courses_canBeModifiedAfterConstruction() {
        Student s = new Student();
        s.getCourses().add("EECS 1012");

        assertThat(s.getCourses()).containsExactly("EECS 1012");
    }

    @Test
    void attendedEvents_addAndClear() {
        Student s = new Student();

        s.getAttendedEventIds().add("event1");
        s.getAttendedEventIds().add("event2");

        assertThat(s.getAttendedEventIds()).containsExactly("event1", "event2");

        s.getAttendedEventIds().clear();
        assertThat(s.getAttendedEventIds()).isEmpty();
    }

    @Test
    void notifications_canBeUpdated() {
        Student s = new Student();

        s.getNotifications().put("email", true);
        s.getNotifications().put("push", false);

        assertThat(s.getNotifications().get("email")).isTrue();
        assertThat(s.getNotifications().get("push")).isFalse();
    }

    @Test
    void privacySettings_canBeUpdated() {
        Student s = new Student();

        s.getPrivacySettings().put("showEmail", false);
        s.getPrivacySettings().put("showCourses", true);

        assertThat(s.getPrivacySettings().get("showEmail")).isFalse();
        assertThat(s.getPrivacySettings().get("showCourses")).isTrue();
    }

    // -----------------------------
    // Defensive copying (if applicable)
    // -----------------------------

    @Test
    void setCourses_replacesListNotAppends() {
        Student s = new Student();
        s.setCourses(new ArrayList<>(List.of("A", "B")));

        assertThat(s.getCourses()).containsExactly("A", "B");

        s.setCourses(new ArrayList<>(List.of("C")));
        assertThat(s.getCourses()).containsExactly("C");
    }

    @Test
    void setCourses_handlesNullByClearingList() {
        Student s = new Student();
        s.setCourses(List.of("A", "B"));

        s.setCourses(null);

        assertThat(s.getCourses()).isEmpty();
    }
}