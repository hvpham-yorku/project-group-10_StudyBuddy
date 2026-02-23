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
    // Remove any existing student with same ID
        StubDatabase.STUDENTS.removeIf(s -> s.getUserId().equals(student.getUserId()));
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

    // Updates the program of a student based on their input
    @Override
    public void updateProgram(String userId, String program) throws Exception {
        Student student = getStudent(userId);
        if (student != null) {
            student.setProgram(program);
    }
}

    // Updates the year of a student based on their input
    @Override
    public void updateYear(String userId, String year) throws Exception {
        Student student = getStudent(userId);
        if (student != null) {
            student.setYear(year);
    }
}

    // Updates the privacy settings of a student based on their choices
    @Override
public void updatePrivacySettings(String studentID, Map<String, Boolean> privacySettings) throws Exception {
    Student student = getStudent(studentID);
    if (student == null) return;

    if (privacySettings.containsKey("showBio"))
        student.setShowBio(privacySettings.get("showBio"));

    if (privacySettings.containsKey("showProgram"))
        student.setShowProgram(privacySettings.get("showProgram"));

    if (privacySettings.containsKey("showYear"))
        student.setShowYear(privacySettings.get("showYear"));

    if (privacySettings.containsKey("showCourses"))
        student.setShowCourses(privacySettings.get("showCourses"));

    if (privacySettings.containsKey("showStudyVibes"))
        student.setShowStudyVibes(privacySettings.get("showStudyVibes"));

    if (privacySettings.containsKey("showEmail"))
        student.setShowEmail(privacySettings.get("showEmail"));
}
    

    @Override
    public void updateProfilePic(String userId, String profilePic) throws Exception {
        Student student = getStudent(userId);
        if(student != null){
            student.setProfilePic(profilePic);
        }
    }
}