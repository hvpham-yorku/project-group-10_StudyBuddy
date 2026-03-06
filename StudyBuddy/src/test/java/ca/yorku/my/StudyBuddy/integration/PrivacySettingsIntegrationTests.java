package integration;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import ca.yorku.my.StudyBuddy.StudyBuddyApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest(classes = StudyBuddyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PrivacySettingsIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUpdatePrivacySettingsStudyVibesNotifications() throws Exception {
        // Update privacy settings
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/students/1/privacy")
                .contentType("application/json")
                .content("{\"privacy\":\"private\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Update study vibes
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/students/1/vibes")
                .contentType("application/json")
                .content("{\"vibes\":\"focused\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Trigger notification
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/notifications")
                .contentType("application/json")
                .content("{\"userId\":1,\"message\":\"Study session starting!\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
