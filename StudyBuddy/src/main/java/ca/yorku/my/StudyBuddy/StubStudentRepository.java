package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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
    public void updateCourses(String userId, List<String> courses) throws Exception {
        Student student = getStudent(userId);
        if (student != null){
            student.setCourses(courses);
        }
    }

    // Updates the study vibe a student inputs in their profile
    @Override
    public void updateStudyVibes(String userId, List<String> studyVibes) throws Exception {
        Student student = getStudent(userId);
        if(student != null){
            student.setStudyVibes(studyVibes);
    }
}

    // Updates the bio of a student based on their input
    @Override
    public void updateBio(String userId, String bio) throws Exception {
        Student student = getStudent(userId);
        if(student != null){
            student.setBio(bio);
        }
    }

    public void updateProgram(String userId, String program) throws Exception {
        Student student = getStudent(userId);
        if (student != null) {
            student.setProgram(program);
    }
}

    public void updateYear(String userId, String year) throws Exception {
        Student student = getStudent(userId);
        if (student != null) {
            student.setYear(year);
    }
}

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updatePrivacySettings(String userId, Map<String, Boolean> privacySettings) throws Exception {
        Student student = getStudent(userId);
        if(student != null){
        student.setPrivacySettings(privacySettings);
        }
    }
}