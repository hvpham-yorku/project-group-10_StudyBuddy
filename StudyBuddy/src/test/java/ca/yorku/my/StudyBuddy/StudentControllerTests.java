package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.controllers.StudentController;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentControllerTests {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AuthRepository authService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    void setup() {
        StubDatabase.STUDENTS.clear();
    }

    @Test
    void getStudent_returnsStudent() throws Exception {
        Student s = new Student("123", "John", "Doe");
        s.setProgram("Computer Science");
        s.setCourses(List.of("EECS 2311", "EECS 3311"));

        when(studentRepository.getStudent("123")).thenReturn(s);

        Student result = studentController.getStudent("123");

        assert result.getFirstName().equals("John");
        assert result.getProgram().equals("Computer Science");
    }

    @Test
    void saveStudent_callsRepository() throws Exception {
        Student s = new Student("1", "Alice", "Smith");
        studentController.saveStudent(s);
        verify(studentRepository).saveStudent(s);
    }

    @Test
    void updateCourses_callsRepository() throws Exception {
        studentController.updateCourses("123", List.of("EECS 2311"));
        verify(studentRepository).updateCourses("123", List.of("EECS 2311"));
    }

    @Test
    void updateAvatar_callsRepository() throws Exception {
        when(authService.verifyFrontendToken("123")).thenReturn("123");
        studentController.updateAvatar("123", Map.of("avatar", "url"));
        verify(studentRepository).updateAvatar("123", "url");
    }

    @Test
    void updateAvatar_handlesNullBody() throws Exception {
        when(authService.verifyFrontendToken("123")).thenReturn("123");
        studentController.updateAvatar("123", null);
        verify(studentRepository).updateAvatar("123", null);
    }

    @Test
    void updateAvatar_rejectsInvalidToken() throws Exception {
        when(authService.verifyFrontendToken("123")).thenReturn(null);
        studentController.updateAvatar("123", Map.of("avatar", "url"));
        verify(studentRepository, never()).updateAvatar(any(), any());
    }
}