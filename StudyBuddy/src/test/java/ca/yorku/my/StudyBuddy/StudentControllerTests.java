package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

class StudentControllerTests {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
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
        studentController.updateAvatar("123", Map.of("avatar", "url"));

        verify(studentRepository).updateAvatar("123", "url");
    }
}
