package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController 
@RequestMapping("/api/studentcontroller")
@CrossOrigin(origins = "*")

// This class allows for the student information to be accessed and modified through API calls
public class StudentController {

	// This allows for the student repository to be used in this class, whether it is the stub or Firestore version
	@Autowired
	private StudentRepository studentRepository;

	// This allows for the session log service to be used in this class
	@Autowired
	private SessionLogService sessionLogService;
	
	// This method allows for all students in the database to be retrieved through an API call
	@GetMapping("/getstudents")
	public ArrayList<Student> getAllStudents() {
		return StubDatabase.STUDENTS;
	}
	
	// This method allows for a student to be retrieved from the database through an API call using their ID
	@GetMapping("/{studentID}")
	public Student getStudent(@PathVariable String studentID) throws Exception {
		return studentRepository.getStudent(studentID);
	}

	// This method allows for a student to be saved to the database through an API call
	@PostMapping("/save")
	public void saveStudent(@RequestBody Student student) throws Exception {
		studentRepository.saveStudent(student);
	}

	// This method allows for a student's courses to be updated in the database through an API call
	@PutMapping("/{studentID}/courses")
	public void updateCourses(@PathVariable String studentID, @RequestBody List<String> courses) throws Exception {
		studentRepository.updateCourses(studentID, courses);
	}

	// This method allows for a student's study vibe to be updated in the database through an API call
	@PutMapping("/{studentID}/study-vibe")
	public void updateStudyVibe(@PathVariable String studentID, @RequestBody String studyVibe) throws Exception {
		studentRepository.updateStudyVibe(studentID, studyVibe);
	}
	
	// This method allows for a student's privacy settings to be updated in the database through an API call
	@PutMapping("/{studentID}/privacy-settings")
	public void updatePrivacySettings(@PathVariable String studentID, @RequestBody Map<String, Boolean> privacySettings) throws Exception {
		studentRepository.updatePrivacySettings(studentID, privacySettings);
	}

	/**
	 * Get a student's session log (all events they have attended)
	 */
	@GetMapping("/getstudent/{studentId}/sessionlog")
	public List<Event> getStudentSessionLog(@PathVariable String studentId) throws ExecutionException, InterruptedException {
		// Find student by ID from StubDatabase
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return new ArrayList<>();
		}
		
		return sessionLogService.getStudentSessionLog(student.getAttendedEventIds());
	}
	
	/**
	 * Get total study time (in minutes) for a student
	 */
	@GetMapping("/getstudent/{studentId}/totalstudytime")
	public long getTotalStudyTime(@PathVariable String studentId) throws ExecutionException, InterruptedException {
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return 0;
		}
		
		List<Event> sessionLog = sessionLogService.getStudentSessionLog(student.getAttendedEventIds());
		return sessionLogService.getTotalStudyMinutes(sessionLog);
	}
	
	/**
	 * Get student's session log filtered by course code
	 */
	@GetMapping("/getstudent/{studentId}/sessionlog/course/{courseCode}")
	public List<Event> getSessionLogByCourse(@PathVariable String studentId, @PathVariable String courseCode) 
			throws ExecutionException, InterruptedException {
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return new ArrayList<>();
		}
		
		List<Event> sessionLog = sessionLogService.getStudentSessionLog(student.getAttendedEventIds());
		return sessionLogService.filterSessionLogByCourse(sessionLog, courseCode);
	}
	
	/**
	 * Mark a student as having attended an event
	 */
	@PostMapping("/getstudent/{studentId}/addeventtosessionlog/{eventId}")
	public Boolean addEventToSessionLog(@PathVariable String studentId, @PathVariable String eventId) {
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return false;
		}
		
		sessionLogService.addEventToSessionLog(student, eventId);
		return true;
	}
}
