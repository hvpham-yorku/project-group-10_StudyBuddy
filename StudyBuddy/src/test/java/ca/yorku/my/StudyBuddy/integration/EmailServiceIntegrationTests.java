package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.when;

import ca.yorku.my.StudyBuddy.StudyBuddyApplication;

@ActiveProfiles("stub")
@SpringBootTest(classes = StudyBuddyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EmailServiceIntegrationTests {
    
    @MockBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setupMock() {
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSendEmailAndErrorHandling() throws Exception {
        // Send email
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/email/send")
                .contentType("application/json")
                .content("{\"to\":\"alex@my.yorku.ca\",\"subject\":\"Test\",\"body\":\"Hello\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        // Error scenario
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/email/send")
                .contentType("application/json")
                .content("{\"to\":\"invalid\",\"subject\":\"Test\",\"body\":\"Hello\"}")
        ).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is4xxClientError());
    }
}
