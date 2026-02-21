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
}