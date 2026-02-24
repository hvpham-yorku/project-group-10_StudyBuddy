package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

// This interface creates methods that allow for information in student database
public interface StudentRepository {

    // Retrieves student by their ID if they exist, otherwise throws an exception
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
    void updateAvatar(String userId, String avatar) throws Exception;

    // Updates the location of a student based on their input
    void updateLocation(String userId, String location) throws Exception;

    // Updates the online status of a student based on their input
    void updateTwoFA(String userId, boolean twoFAEnabled) throws Exception;

    // Updates the auto timeout duration of a student based on their input
    void updateAutoTimeout(String userId, int autoTimeout) throws Exception;

    // Updates the two-factor authentication setting of a student based on their input
    void updateOnlineStatus(String userId, boolean isOnline) throws Exception;

    // Updates the notifications settings of a student based on their input
    void updateNotifications(String userId, Map<String, Boolean> notifications) throws Exception;
}