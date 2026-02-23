package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

// This interface creates methods that allow for information in student database
public interface StudentRepository {

    // Retrieves student by their ID, if they exist, otherwise throws an exception
    Student getStudent(String userId) throws Exception;

    // Saves a student to the database using their ID
    void saveStudent(Student student) throws Exception;

    // Updates the courses a student is currently enrolled in
    void updateCourses(String userId, List<String> courses) throws Exception;

    // Updates the study vibe a student inputs in their profile
    void updateStudyVibes(String userId, List<String> studyVibes) throws Exception;

    // Updates the privacy settings of a student based on their choices
    void updatePrivacySettings(String userId, Map<String, Boolean> privacySettings) throws Exception;

    // Updates the bio of a student based on their input
    void updateBio(String userId, String bio) throws Exception;

    // Updates the year of a student based on their input
    void updateYear(String userId, String year) throws Exception;

    // Updates the program of a student based on their input
    void updateProgram(String userId, String program) throws Exception;

    // Updates the profile picture of a student based on their input
    void updateProfilePic(String userId, String profilePic) throws Exception;
}