package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.yorku.my.StudyBuddy.StudyBuddyApplication;

@ActiveProfiles("stub")
@SpringBootTest
@AutoConfigureMockMvc
public class PrivacySettingsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // ------------------------------------------------------------
    // PRIVACY SETTINGS TESTS
    // ------------------------------------------------------------

    @Test
    void updatePrivacySettings_valid() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/privacy-settings")
                .contentType("application/json")
                .content("{\"privacy\": true}")
        ).andExpect(status().isInternalServerError()); 
        // Student "1" does not exist in stub → 500
    }

    @Test
    void updatePrivacySettings_invalidField() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/privacy-settings")
                .contentType("application/json")
                .content("{\"privacy\": \"notARealSetting\"}")
        ).andExpect(status().isBadRequest()); 
        // Wrong type → Spring rejects → 400
    }

    @Test
    void updatePrivacySettings_missingBody() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/privacy-settings")
                .contentType("application/json")
        ).andExpect(status().isBadRequest()); 
        // Missing body → 400
    }

    @Test
    void updatePrivacySettings_emptyJson() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/privacy-settings")
                .contentType("application/json")
                .content("{}")
        ).andExpect(status().isBadRequest()); 
        // Empty JSON → validation fails → 400
    }

    @Test
    void updatePrivacySettings_studentNotFound() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/999/privacy-settings")
                .contentType("application/json")
                .content("{\"privacy\": true}")
        ).andExpect(status().isInternalServerError()); 
        // Stub throws → 500
    }

    @Test
    void updatePrivacySettings_invalidIdFormat() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/abc/privacy-settings")
                .contentType("application/json")
                .content("{\"privacy\": true}")
        ).andExpect(status().isInternalServerError()); 
        // Stub throws → 500
    }

    @Test
    void updatePrivacySettings_wrongMethod() throws Exception {
        mockMvc.perform(
            get("/api/studentcontroller/1/privacy-settings")
        ).andExpect(status().isMethodNotAllowed());
    }

    // ------------------------------------------------------------
    // STUDY VIBES TESTS
    // ------------------------------------------------------------

    @Test
    void updateVibes_valid() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[\"focused\"]")
        ).andExpect(status().isInternalServerError()); 
        // Student "1" not found → 500
    }

    @Test
    void updateVibes_invalidValue() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[\"unknownMood\"]")
        ).andExpect(status().isInternalServerError()); 
        // Stub throws → 500
    }

    @Test
    void updateVibes_emptyJson() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[]")
        ).andExpect(status().isBadRequest()); 
        // Empty list → validation fails → 400
    }

    @Test
    void updateVibes_wrongMethod() throws Exception {
        mockMvc.perform(
            post("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[\"focused\"]")
        ).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void updateVibes_excessivelyLongValue() throws Exception {
        String longValue = "a".repeat(5000);

        mockMvc.perform(
            put("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[\"" + longValue + "\"]")
        ).andExpect(status().isInternalServerError()); 
        // Stub throws → 500
    }

    // ------------------------------------------------------------
    // MULTI-STEP FLOW TEST
    // ------------------------------------------------------------

    @Test
    void testUpdatePrivacySettingsStudyVibes() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/1/privacy-settings")
                .contentType("application/json")
                .content("{\"privacy\": true}")
        ).andExpect(status().isInternalServerError());

        mockMvc.perform(
            put("/api/studentcontroller/1/study-vibes")
                .contentType("application/json")
                .content("[\"focused\"]")
        ).andExpect(status().isInternalServerError());
    }
}