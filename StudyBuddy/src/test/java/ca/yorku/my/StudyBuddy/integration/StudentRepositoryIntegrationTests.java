package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.yorku.my.StudyBuddy.StudyBuddyApplication;

@ActiveProfiles("stub")
@SpringBootTest(
    classes = StudyBuddyApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
public class StudentRepositoryIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // ------------------------------------------------------------
    // CREATE TESTS
    // ------------------------------------------------------------

    @Test
    void createStudent_validRequest_returns200() throws Exception {
        mockMvc.perform(
            post("/api/students")
                .contentType("application/json")
                .content("{\"email\":\"sarah@my.yorku.ca\",\"name\":\"Sarah\"}")
        ).andExpect(status().isOk());
    }

    @Test
    void createStudent_missingFields_stillReturns200() throws Exception {
        mockMvc.perform(
            post("/api/students")
                .contentType("application/json")
                .content("{\"email\":\"noName@my.yorku.ca\"}")
        ).andExpect(status().isOk());
    }

    @Test
    void createStudent_invalidJson_returns200() throws Exception {
        mockMvc.perform(
            post("/api/students")
                .contentType("application/json")
                .content("{bad json}")
        ).andExpect(status().isOk());
    }

    @Test
    void createStudent_unsupportedMediaType_returns200() throws Exception {
        mockMvc.perform(
            post("/api/students")
                .contentType("text/plain")
                .content("hello")
        ).andExpect(status().isOk());
    }

    // ------------------------------------------------------------
    // READ TESTS
    // ------------------------------------------------------------

    @Test
    void getStudent_existingOrNot_returns200() throws Exception {
        mockMvc.perform(get("/api/students/9999"))
            .andExpect(status().isOk());
    }

    // ------------------------------------------------------------
    // UPDATE TESTS
    // ------------------------------------------------------------

    @Test
    void updateStudent_validRequest_returns200() throws Exception {
        mockMvc.perform(
            put("/api/students/2")
                .contentType("application/json")
                .content("{\"name\":\"Updated Name\"}")
        ).andExpect(status().isOk());
    }

    @Test
    void updateStudent_invalidJson_returns200() throws Exception {
        mockMvc.perform(
            put("/api/students/2")
                .contentType("application/json")
                .content("{bad json}")
        ).andExpect(status().isOk());
    }

    @Test
    void updateStudent_missingBody_returns200() throws Exception {
        mockMvc.perform(
            put("/api/students/2")
                .contentType("application/json")
                .content("")
        ).andExpect(status().isOk());
    }

    // ------------------------------------------------------------
    // DELETE TESTS
    // ------------------------------------------------------------

    @Test
    void deleteStudent_alwaysReturns200() throws Exception {
        mockMvc.perform(delete("/api/students/12345"))
            .andExpect(status().isOk());
    }
}