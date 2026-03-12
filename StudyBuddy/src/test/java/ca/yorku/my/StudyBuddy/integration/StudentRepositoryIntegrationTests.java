package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import ca.yorku.my.StudyBuddy.StudyBuddyApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest(classes = StudyBuddyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class StudentRepositoryIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testStudentRepositoryCRUDAndController() throws Exception {
        // Create student
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/students")
                .contentType("application/json")
                .content("{\"email\":\"sarah@my.yorku.ca\",\"name\":\"Sarah\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Retrieve student
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/students/2")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Update student
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/students/2")
                .contentType("application/json")
                .content("{\"name\":\"Sarah Updated\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Delete student
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/students/2")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
