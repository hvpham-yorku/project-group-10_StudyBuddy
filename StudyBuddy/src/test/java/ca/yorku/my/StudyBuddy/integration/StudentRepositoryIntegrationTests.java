package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    // CREATE / SAVE TESTS
    // ------------------------------------------------------------

    @Test
    void saveStudent_validRequest_returns200() throws Exception {
        // Points to the actual save endpoint in StudentController
        mockMvc.perform(
            post("/api/studentcontroller/save")
                .contentType("application/json")
                .content("{\"userId\":\"user123\",\"firstName\":\"Sarah\",\"lastName\":\"Connor\",\"email\":\"sarah@my.yorku.ca\"}")
        ).andExpect(status().isOk());
    }

    // ------------------------------------------------------------
    // READ TESTS
    // ------------------------------------------------------------

    @Test
    void getStudent_nonExisting_returns404() throws Exception {
        // Asking for a non-existent student should correctly return a 404 Not Found
        mockMvc.perform(get("/api/studentcontroller/9999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getStudent_existing_returns200() throws Exception {
        // First, create a user in the stub database to ensure they exist
        mockMvc.perform(
            post("/api/studentcontroller/save")
                .contentType("application/json")
                .content("{\"userId\":\"testUser1\",\"firstName\":\"John\",\"lastName\":\"Doe\"}")
        ).andExpect(status().isOk());

        // Now, fetch that exact user and expect a 200 OK
        mockMvc.perform(get("/api/studentcontroller/testUser1"))
            .andExpect(status().isOk());
    }
    
    @Test
    void getAllStudents_returns200() throws Exception {
        mockMvc.perform(get("/api/studentcontroller/getstudents"))
            .andExpect(status().isOk());
    }

}