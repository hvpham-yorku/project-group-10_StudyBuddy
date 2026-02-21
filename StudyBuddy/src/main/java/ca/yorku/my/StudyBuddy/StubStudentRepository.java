package ca.yorku.my.StudyBuddy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import scala.collection.immutable.List;

@Service
@Profile("stub")

// This class allows for testing using stub database rather than Firestore
public class StubStudentRepository implements StudentRepository {

    // Retrieves the student from the stub database based on ID
    @Override
    public Student getStudent(String userId) throws Exception {
        return StubDatabase.STUDENTS.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new Exception("Student not found"));
    }

    // Saves the student to the stub database
    @Override
    public void saveStudent(Student student) throws Exception {
        StubDatabase.STUDENTS.add(student);
    }

    // Updates the courses a student is currently enrolled in
    @Override
    public void updateCourses(String userId, List<Course> courses) throws Exception {
        Student student = getStudent(userId);
        if (student != null){
            student.setCourses(courses);
        }
    }

    // Updates the study vibe a student inputs in their profile
    @Override
    public void updateStudyVibe(String userId, Object studyVibe) throws Exception {
        Student student = getStudent(userId);
        student.setStudyVibe(studyVibe);
    }

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updatePrivacySettings(String userId, Object privacySettings) throws Exception {
        Student student = getStudent(userId);
        student.setPrivacySettings(privacySettings);
    }
}