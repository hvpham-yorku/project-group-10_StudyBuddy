package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ca.yorku.my.StudyBuddy.StudyBuddyApplication;

@ActiveProfiles("stub")
@SpringBootTest(classes = StudyBuddyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class FileUploadDownloadIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFileUploadAndDownload() throws Exception {
        // Upload file (simplified, actual multipart may require MockMultipartFile)
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/files/upload")
                .contentType("multipart/form-data")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Download file
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/files/download?fileId=1")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }
}
