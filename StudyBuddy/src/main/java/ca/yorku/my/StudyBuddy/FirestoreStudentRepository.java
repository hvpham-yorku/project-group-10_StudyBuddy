package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service 
@Profile("firestore")

// This class allows information from the Firestore database to be accessed and modified
public class FirestoreStudentRepository implements StudentRepository {

    private Firestore db = FirestoreClient.getFirestore();

    // Retrieves a student from Firestore using ID, if they exist, otherwise throws an exception
    @Override
    public Student getStudent(String userId) throws Exception {
        DocumentSnapshot doc = db.collection("students").document(userId).get().get();
        if (doc.exists()) {
            return doc.toObject(Student.class);
        } else {
            throw new Exception("Student not found");
        }
    }
    
    // Saves a student to Firestore database using student ID
    @Override
    public void saveStudent(Student student) throws Exception {
        db.collection("students").document(student.getUserId()).set(student).get();
    }

    // Updates the courses a student is currently enrolled in
    @Override
    public void updateCourses(String userId, List<String> courses) throws Exception {
        db.collection("students").document(userId).update("enrolledCourses", courses).get();
    }

    // Updates the study vibe a student inputs in their profile
    @Override
    public void updateStudyVibes(String userId, List<String> studyVibes) throws Exception {
        db.collection("students").document(userId).update("studyVibes", studyVibes).get();
    }

    // Updates the bio of a student based on their input
    @Override
    public void updateBio(String userId, String bio) throws Exception {
    db.collection("students").document(userId).update("bio", bio).get();
}

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updatePrivacySettings(String userId, Map<String, Boolean> privacySettings) throws Exception {
        db.collection("students").document(userId).update("privacySettings", privacySettings).get();
    }

    // Updates the profile picture of a student based on their input
    @Override
    public void updateYear(String userId, String year) throws Exception {
        db.collection("students").document(userId).update("year", year).get();
    }

    // Updates the program of a student based on their input
    @Override
    public void updateProgram(String userId, String program) throws Exception {
        db.collection("students").document(userId).update("program", program).get();
    }

    // Updates the profile picture of a student based on their input
    @Override
    public void updateAvatar(String userId, String avatar) throws Exception {
        db.collection("students").document(userId).update("avatar", avatar).get();
    }   

    // Updates the location of a student based on their input
    @Override
    public void updateLocation(String userId, String location) throws Exception {
        db.collection("students").document(userId).update("location", location).get();
    }

    // Updates the two-factor authentication setting of a student based on their input
    @Override
    public void updateTwoFAEnabled(String userId, Boolean twoFAEnabled) throws Exception {
        db.collection("students").document(userId).update("twoFAEnabled", twoFAEnabled).get();
    }

    // Updates the auto timeout duration of a student based on their input
    @Override
    public void updateAutoTimeout(String userId, int autoTimeout) throws Exception {
        db.collection("students").document(userId).update("autoTimeout", autoTimeout).get();
    }

    // Updates the online status of a student based on their input
    @Override
    public void updateOnlineStatus(String userId, Boolean isOnline) throws Exception {
        db.collection("students").document(userId).update("isOnline", isOnline).get();
    }

    // Updates the notifications settings of a student based on their input
    @Override
    public void updateNotifications(String userId, Map<String, Boolean> notifications) throws Exception {
        db.collection("students").document(userId).update("notifications", notifications).get();
    }
}