package ca.yorku.my.StudyBuddy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FirestoreStudentRepositoryIntegrationTests {

    @Autowired
    private FirestoreStudentRepository repo;

    @Test
    void createReadUpdateDeleteStudentAgainstActualFirestore() throws Exception {
        Path firebaseKey = Path.of("src/main/resources/serviceAccountKey.json");
        Assumptions.assumeTrue(
                Files.exists(firebaseKey),
                "Skipping Firestore integration test because serviceAccountKey.json is missing"
        );

        String id = "integration-" + UUID.randomUUID().toString().substring(0, 8);

        // CREATE
        Student s = new Student(id, "John", "Doe");
        repo.saveStudent(s);

        // READ
        Student fetched = repo.getStudent(id);
        assertNotNull(fetched);
        assertEquals(id, fetched.getUserId());

        // UPDATE: Program
        repo.updateProgram(id, "Computer Science");
        assertEquals("Computer Science", repo.getStudent(id).getProgram());

        // UPDATE: Courses
        repo.updateCourses(id, List.of("EECS 2311", "EECS 3311"));
        assertEquals(List.of("EECS 2311", "EECS 3311"), repo.getStudent(id).getCourses());

        // UPDATE: Study Vibes
        repo.updateStudyVibes(id, List.of("Quiet", "Lo-fi"));
        assertEquals(List.of("Quiet", "Lo-fi"), repo.getStudent(id).getStudyVibes());

        // UPDATE: Bio
        repo.updateBio(id, "Hello world");
        assertEquals("Hello world", repo.getStudent(id).getBio());

        // UPDATE: Privacy Settings
        repo.updatePrivacySettings(id, Map.of("showBio", true));
        assertTrue(repo.getStudent(id).getPrivacySettings().get("showBio"));

        // UPDATE: Avatar
        repo.updateAvatar(id, "https://example.com/avatar.png");
        assertEquals("https://example.com/avatar.png", repo.getStudent(id).getAvatar());

        // UPDATE: Location
        repo.updateLocation(id, "Toronto");
        assertEquals("Toronto", repo.getStudent(id).getLocation());

        // UPDATE: Two-Factor Auth
        repo.updateTwoFAEnabled(id, false);
        assertFalse(repo.getStudent(id).isTwoFAEnabled());

        // UPDATE: Auto Timeout
        repo.updateAutoTimeout(id, 45);
        assertEquals(45, repo.getStudent(id).getAutoTimeout());

        // UPDATE: Online Status
        repo.updateOnlineStatus(id, false);
        assertFalse(repo.getStudent(id).getIsOnline());

        // UPDATE: Notifications
        repo.updateNotifications(id, Map.of("chatMessages", true));
        assertTrue(repo.getStudent(id).getNotifications().get("chatMessages"));

        // DELETE
        repo.deleteStudent(id);

        // VERIFY (repo auto-creates missing students)
        Student recreated = repo.getStudent(id);
        assertNotNull(recreated);
        assertEquals(id, recreated.getUserId());
    }
}