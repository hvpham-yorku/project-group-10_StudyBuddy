package ca.yorku.my.StudyBuddy.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ca.yorku.my.StudyBuddy.SessionLogService;
import ca.yorku.my.StudyBuddy.daos.ChatDAO;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@SpringBootTest(
    classes = {
        ca.yorku.my.StudyBuddy.controllers.StudentController.class
    }
)
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private AuthRepository authService;

    @MockBean
    private SessionLogService sessionLogService;

    @MockBean
    private ChatDAO chatDAO;

    // ------------------------------------------------------------
    // STUDY VIBES TESTS
    // ------------------------------------------------------------

    @Test
    void updateStudyVibes_handlesNullList() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("null")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updateStudyVibes(any(), any());
    }

    @Test
    void updateStudyVibes_handlesEmptyList() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("[]")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updateStudyVibes(any(), any());
    }

    @Test
    void updateStudyVibes_acceptsValidList() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("[\"focused\", \"motivated\"]")
        ).andExpect(status().isOk());

        verify(studentRepository).updateStudyVibes(any(), any());
    }

    @Test
    void updateStudyVibes_rejectsWrongType() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"not\": \"a list\"}")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updateStudyVibes(any(), any());
    }

    @Test
    void updateStudyVibes_rejectsNonJson() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/study-vibes")
                .contentType(MediaType.TEXT_PLAIN)
                .content("hello")
        ).andExpect(status().isUnsupportedMediaType());
    }

    // ------------------------------------------------------------
    // PRIVACY SETTINGS TESTS
    // ------------------------------------------------------------

    @Test
    void updatePrivacySettings_allowsEmptyMap() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{}")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updatePrivacySettings(any(), any());
    }

    @Test
    void updatePrivacySettings_handlesNullMap() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("null")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updatePrivacySettings(any(), any());
    }

    @Test
    void updatePrivacySettings_acceptsValidMap() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"showEmail\": true}")
        ).andExpect(status().isOk());

        verify(studentRepository).updatePrivacySettings(any(), any());
    }

    @Test
    void updatePrivacySettings_rejectsWrongValueType() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"showEmail\": \"yes\"}")
        ).andExpect(status().isBadRequest());

        verify(studentRepository, never()).updatePrivacySettings(any(), any());
    }

    @Test
    void updatePrivacySettings_rejectsNonJson() throws Exception {
        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.TEXT_PLAIN)
                .content("hello")
        ).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updatePrivacySettings_handlesRepositoryException() throws Exception {
        doThrow(new RuntimeException("DB error"))
            .when(studentRepository).updatePrivacySettings(any(), any());

        mockMvc.perform(
            put("/api/studentcontroller/123/privacy-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"showEmail\": true}")
        ).andExpect(status().isInternalServerError());
    }
}