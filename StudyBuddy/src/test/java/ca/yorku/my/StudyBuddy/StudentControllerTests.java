package ca.yorku.my.StudyBuddy;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.classes.StudentRepository;
import ca.yorku.my.StudyBuddy.controllers.StudentController;

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

    @Test
    void updateStudyVibes_callsRepository() throws Exception {
        studentController.updateStudyVibes("123", List.of("Quiet", "Lo-fi"));

        verify(studentRepository).updateStudyVibes("123", List.of("Quiet", "Lo-fi"));
    }

    @Test
    void updatePrivacySettings_callsRepository() throws Exception {
        Map<String, Boolean> settings = Map.of("showBio", true, "showCourses", false);

        studentController.updatePrivacySettings("123", settings);

        verify(studentRepository).updatePrivacySettings("123", settings);
    }

    @Test
    void getStudent_returnsNullWhenNotFound() throws Exception {
        when(studentRepository.getStudent("999")).thenReturn(null);

        Student result = studentController.getStudent("999");

        assert result == null;
    }

    @Test
    void updateCourses_allowsEmptyList() throws Exception {
        studentController.updateCourses("123", List.of());

        verify(studentRepository).updateCourses("123", List.of());
    }

    @Test
    void updateAvatar_ignoresMissingAvatarKey() throws Exception {
        studentController.updateAvatar("123", Map.of("wrongKey", "value"));

        verify(studentRepository).updateAvatar("123", null);
    }

    @Test
    void updateStudyVibes_handlesNullList() throws Exception {
        studentController.updateStudyVibes("123", null);

        verify(studentRepository).updateStudyVibes("123", null);
    }

    @Test
    void updatePrivacySettings_allowsEmptyMap() throws Exception {
        studentController.updatePrivacySettings("123", Map.of());

        verify(studentRepository).updatePrivacySettings("123", Map.of());
    }

    @Test
    void getStudent_handlesWhitespaceId() throws Exception {
        when(studentRepository.getStudent("   ")).thenReturn(null);

        Student result = studentController.getStudent("   ");

        assert result == null;
    }

    @Test
    void updateCourses_doesNotMutateInputList() throws Exception {
        List<String> courses = List.of("EECS 2311");

        studentController.updateCourses("123", courses);

        assert courses.size() == 1;
        verify(studentRepository).updateCourses("123", courses);
    }
}
