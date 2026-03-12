package ca.yorku.my.StudyBuddy.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private AuthRepository authService;

    @Test
    void updateStudyVibes_handlesNullList() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType("application/json")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updateStudyVibes(any(), any());
    }

    @Test
    void updatePrivacySettings_allowsEmptyMap() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType("application/json")
                .content("{}")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updatePrivacySettings(any(), any());
    }

    @Test
    void updatePrivacySettings_handlesNullMap() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType("application/json")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updatePrivacySettings(any(), any());
    }
}