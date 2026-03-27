package ca.yorku.my.StudyBuddy;
import ca.yorku.my.StudyBuddy.classes.Student;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StubStudentRepositoryTests {

    private StubStudentRepository repo;

    @BeforeEach
    void setup() {
        repo = new StubStudentRepository();
        StubDatabase.STUDENTS.clear();
    }

    @Test
    void saveStudent_savesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        repo.saveStudent(s);

        Student result = repo.getStudent("123");
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getStudent_returnsSavedStudent() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        Student result = repo.getStudent("123");
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getStudent_throwsWhenNotFound() {
        assertThrows(Exception.class, () -> repo.getStudent("999"));
    }

    @Test
    void updateCourses_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateCourses("123", List.of("EECS 2311"));
        assertEquals(List.of("EECS 2311"), s.getCourses());
    }

    @Test
    void updateAvatar_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateAvatar("123", "avatarUrl");
        assertEquals("avatarUrl", s.getAvatar());
    }

    @Test
    void updateStudyVibes_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateStudyVibes("123", List.of("Quiet"));
        assertEquals(List.of("Quiet"), s.getStudyVibes());
    }

    @Test
    void updatePrivacySettings_mergesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        s.setPrivacySettings(new java.util.HashMap<>(Map.of("showBio", false)));
        StubDatabase.STUDENTS.add(s);

        repo.updatePrivacySettings("123", Map.of("showBio", true));
        assertEquals(true, s.getPrivacySettings().get("showBio"));
    }

    @Test
    void updateNotifications_mergesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        s.setNotifications(new java.util.HashMap<>(Map.of("email", false)));        
        StubDatabase.STUDENTS.add(s);

        repo.updateNotifications("123", new java.util.HashMap<>(Map.of("email", true)));            
        assertEquals(true, s.getNotifications().get("email"));
    }

    @Test
    void updateOnlineStatus_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateOnlineStatus("123", true);
        assertTrue(s.getIsOnline());
    }

    @Test
    void updateAutoTimeout_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateAutoTimeout("123", 45);
        assertEquals(45, s.getAutoTimeout());
    }

    @Test
    void updateTwoFAEnabled_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateTwoFAEnabled("123", true);
        assertTrue(s.isTwoFAEnabled());
    }

    @Test
    void updateLocation_updatesCorrectly() throws Exception {
        Student s = new Student("123", "John", "Doe");
        StubDatabase.STUDENTS.add(s);

        repo.updateLocation("123", "Toronto");
        assertEquals("Toronto", s.getLocation());
    }
}