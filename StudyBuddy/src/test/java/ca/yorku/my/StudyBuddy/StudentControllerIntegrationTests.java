package ca.yorku.my.StudyBuddy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stub")
class StudentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        StubDatabase.STUDENTS.clear();
        Student s = new Student("123", "John", "Doe");
        s.setAttendedEventIds(List.of("E1", "E2"));
        StubDatabase.STUDENTS.add(s);
    }

    // -----------------------------
    // Profile Update
    // -----------------------------
    @Test
    void updateProfile_updatesSuccessfully() throws Exception {
        String json = """
            {
                "bio": "Hello world",
                "program": "Computer Science",
                "year": "3"
            }
        """;

        mockMvc.perform(
                post("/api/studentcontroller/profile/update/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Courses
    // -----------------------------
    @Test
    void updateCourses_updatesSuccessfully() throws Exception {
        String json = """
            ["EECS 2311", "EECS 3311"]
        """;

        mockMvc.perform(
                put("/api/studentcontroller/123/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Study Vibes
    // -----------------------------
    @Test
    void updateStudyVibes_updatesSuccessfully() throws Exception {
        String json = """
            ["Quiet", "Lo-fi"]
        """;

        mockMvc.perform(
                put("/api/studentcontroller/123/study-vibes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Privacy Settings
    // -----------------------------
    @Test
    void updatePrivacySettings_updatesSuccessfully() throws Exception {
        String json = """
            { "showBio": true }
        """;

        mockMvc.perform(
                put("/api/studentcontroller/123/privacy-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Avatar
    // -----------------------------
    @Test
    void updateAvatar_updatesSuccessfully() throws Exception {
        String json = """
            { "avatar": "https://example.com/avatar.png" }
        """;

        mockMvc.perform(
                put("/api/studentcontroller/123/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Get Student
    // -----------------------------
    @Test
    void getStudent_returnsSuccessfully() throws Exception {
        mockMvc.perform(
                get("/api/studentcontroller/123")
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Get All Students
    // -----------------------------
    @Test
    void getAllStudents_returnsSuccessfully() throws Exception {
        mockMvc.perform(
                get("/api/studentcontroller/getstudents")
        ).andExpect(status().isOk());
    }

    // -----------------------------
    // Session Log
    // -----------------------------
    @Test
    void getStudentSessionLog_returnsSuccessfully() throws Exception {
        mockMvc.perform(
                get("/api/studentcontroller/getstudent/123/sessionlog")
        ).andExpect(status().isOk());
    }

    @Test
    void getTotalStudyTime_returnsSuccessfully() throws Exception {
        mockMvc.perform(
                get("/api/studentcontroller/getstudent/123/totalstudytime")
        ).andExpect(status().isOk());
    }

    @Test
    void getSessionLogByCourse_returnsSuccessfully() throws Exception {
        mockMvc.perform(
                get("/api/studentcontroller/getstudent/123/sessionlog/course/EECS2311")
        ).andExpect(status().isOk());
    }
}