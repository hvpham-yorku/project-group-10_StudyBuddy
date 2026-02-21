package ca.yorku.my.StudyBuddy;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**  This class manages the session logs of students. It updates the user's session  
*    when an event is created or joined. It includes information such as duration and location
*/
@Service
public class StudentService {

    private static final String COLLECTION = "students";

    /**
     * Retrieves student by a specific ID
     */
    public Student getStudentById(String studentId) throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        DocumentSnapshot doc = db.collection(COLLECTION).document(studentId).get().get();
        
        if (doc.exists()) {
        	return doc.toObject(Student.class);
        } else {
        	return null;
        }
    }
    
    /**
     * Get all students
     */
    public List<Student> getAllStudents() throws ExecutionException, InterruptedException {
    	Firestore db = FirestoreClient.getFirestore();
    	
    	ApiFuture<QuerySnapshot> future = db.collection(COLLECTION).get();
    	List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    	
    	List<Student> students = new ArrayList<>();
    	for (QueryDocumentSnapshot doc : documents) {
    		Student student = doc.toObject(Student.class);
    		student.setUserId(doc.getId());
    		students.add(student);
    	}
    	
    	return students;
    }
    
    /**
     * Create a student
     */
    public Student createStudent(Student student) throws ExecutionException, InterruptedException {
    	
    	Firestore db = FirestoreClient.getFirestore();
    	
    	ApiFuture<DocumentReference> future = db.collection(COLLECTION).add(student);
    	DocumentReference docRef = future.get();
    	student.setUserId(docRef.getId());
    	docRef.set(student).get();
    	return student;
    }
}
