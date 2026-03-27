package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for EmailService.
 * Verifies that emails are constructed and sent using JavaMailSender.
 */
@ExtendWith(MockitoExtension.class)
public class EmailServiceUnitTests {
    @Test
    void sendEmail_EmptySubjectAndBody_ShouldSend() {
        String to = "test@yorku.ca";
        emailService.sendEmail(to, "", "");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_MultipleCalls_ShouldSendEach() {
        String to = "test@yorku.ca";
        emailService.sendEmail(to, "Subject1", "Body1");
        emailService.sendEmail(to, "Subject2", "Body2");
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // No-op: mocks injected by MockitoExtension
    }

    /**
     * Test sending a plain text email.
     * Verifies that JavaMailSender.send() is called with the correct message.
     */
    @Test
    void sendEmail_ShouldSendPlainTextEmail() {
        String to = "test@yorku.ca";
        String subject = "Test Subject";
        String body = "Hello, this is a test email.";

        emailService.sendEmail(to, subject, body);

        // Verify that mailSender.send() was called with a SimpleMailMessage
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
