package ca.yorku.my.StudyBuddy.controllers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.UpdateProfileRequestDTO;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@RestController 
@RequestMapping("/api/studentcontroller")
@CrossOrigin(origins = "*")

// This class allows for the student information to be accessed and modified through API calls
public class StudentController {

	// This allows for the student repository to be used in this class, whether it is the stub or Firestore version
	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
    private AuthRepository authService;

	// This allows for the session log service to be used in this class
	// @Autowired
	// private StudentService studentService;
	
	// This method allows for a student's profile to be updated through an API call. It checks which fields are included in the request and updates those specific fields in the database	
	@PostMapping("/profile/update")
	public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody UpdateProfileRequestDTO req) {
	    try {
	        // Securely get the user's ID from the token!
	        String studentID = authService.verifyFrontendToken(authHeader);

	        // Keep all your existing if-statements here...
	        if (req.courses() != null) {
	            studentRepository.updateCourses(studentID, req.courses());
	        }
	        
	        if (req.courses() != null) {
	            studentRepository.updateCourses(studentID, req.courses());
	        }

	        if (req.studyVibes() != null) {
	            studentRepository.updateStudyVibes(studentID, req.studyVibes());
	        }

	        if (req.privacySettings() != null) {
	            studentRepository.updatePrivacySettings(studentID, req.privacySettings());
	        }

	        if (req.bio() != null) {
	            studentRepository.updateBio(studentID, req.bio());
	        }

	    	if (req.year() != null) {
	    		studentRepository.updateYear(studentID, req.year());
	    	}

	    	if (req.program() != null) {
	    		studentRepository.updateProgram(studentID, req.program());
	    	}

	    	if (req.avatar() != null) {
	    		studentRepository.updateAvatar(studentID, req.avatar());
	    	}

	    	if (req.location() != null) {
	    		studentRepository.updateLocation(studentID, req.location());
	    	}

	    	if (req.notifications() != null) {
	    		studentRepository.updateNotifications(studentID, req.notifications());
	    	}

	    	if(req.twoFAEnabled() != null) {
	    		studentRepository.updateTwoFAEnabled(studentID, req.twoFAEnabled());
	    	}

	    	if(req.autoTimeout() != 0) {
	    		studentRepository.updateAutoTimeout(studentID, req.autoTimeout());
	    	}

	    	if(req.isOnline() != null) {
	    		studentRepository.updateOnlineStatus(studentID, req.isOnline());
	    	}

	        return ResponseEntity.ok("Profile updated");
	    } catch (Exception e) {
	        return ResponseEntity.status(401).body("Unauthorized");
	    }
	}
	

	@GetMapping("/profile")
	public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
	    try {
	        String uid = authService.verifyFrontendToken(authHeader);
	        Student student = studentRepository.getStudent(uid);
	        return ResponseEntity.ok(student);
	    } catch (Exception e) {
	        return ResponseEntity.status(401).body("Unauthorized");
	    }
	}

	
	// This method allows for all students in the database to be retrieved through an API call
	@GetMapping("/getstudents")
	public List<Student> getAllStudents() {
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

	@PostMapping("/{studentID}/study-vibes")
	public ResponseEntity<?> blockStudyVibesPOST() {
    	return ResponseEntity.status(405).build();
	}

	// This method allows for a student's study vibe to be updated in the database through an API call
	@PutMapping("/{studentID}/study-vibes")
	public ResponseEntity<?> updateStudyVibes(@PathVariable String studentID, @RequestBody(required = false) List<String> vibes) {
    	if (vibes == null || vibes.isEmpty() || vibes.stream().anyMatch(v -> v == null || v.isBlank())) {
    		return ResponseEntity.badRequest().build();
		}

    	try {
        	studentRepository.updateStudyVibes(studentID, vibes);
        	return ResponseEntity.ok().build();
    	} catch (Exception e) {
        	return ResponseEntity.internalServerError().build();
    	}
	}
	
	
	@GetMapping("/{studentID}/privacy-settings")
	public ResponseEntity<?> blockPrivacySettingsGET() {
    	return ResponseEntity.status(405).build();
	}

	// This method allows for a student's privacy settings to be updated in the database through an API call
	@PutMapping("/{studentID}/privacy-settings")
	public ResponseEntity<?> updatePrivacySettings(@PathVariable String studentID, @RequestBody(required = false) Map<String, Boolean> privacySettings) {
    	// Missing body
    	if (privacySettings == null) {
        	return ResponseEntity.badRequest().build();
    	}

    	// Missing field
    	if (!privacySettings.containsKey("privacy")) {
        	return ResponseEntity.badRequest().build();
    	}

    	// Wrong type (Spring already rejects strings, but this is safe)
    	if (!(privacySettings.get("privacy") instanceof Boolean)) {
        	return ResponseEntity.badRequest().build();
   	 	}

    	try {
        	studentRepository.updatePrivacySettings(studentID, privacySettings);
        	return ResponseEntity.ok().build();
    	} catch (Exception e) {
        	return ResponseEntity.internalServerError().build();
    	}
	}

	

	
	// Get a student's session log (all events they have attended)
	// TODO: Modify this
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
		return null;
	}
	
	
	// Get total study time (in minutes) for a student
	// TODO: Return number of students (depends on student schemas)
	@GetMapping("/getstudent/{studentId}/totalstudytime")
	public long getTotalStudyTime(@PathVariable String studentId) throws ExecutionException, InterruptedException {
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return 0;
		}
		return 0;
	}
	
	
	// Get student's session log filtered by course code
	// TODO: Return the student's session log
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
		return null;
		
		// List<Event> sessionLog = sessionLogService.getStudentSessionLog(student.getAttendedEventIds());
		// return sessionLogService.filterSessionLogByCourse(sessionLog, courseCode);
	}
	
	
	// Mark a student as having attended an event 
	// TODO: Return the events that the student went to
	@PostMapping("/getstudent/{studentId}/addeventtosessionlog/{eventId}")
	public Boolean addEventToSessionLog(@PathVariable String studentId, @PathVariable String eventId) {
		Student student = StubDatabase.STUDENTS.stream()
			.filter(s -> s.getUserId().equals(studentId))
			.findFirst()
			.orElse(null);
		
		if (student == null) {
			return false;
		}
		return null;
		
		//sessionLogService.addEventToSessionLog(student, eventId);
		//return true;
	}

	// This method allows for a student's profile picture to be updated in the database through an API call
	@PutMapping("/profile/avatar")
	public void updateAvatar(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> body) throws Exception {

    String verifiedId = authService.verifyFrontendToken(authHeader);

    if (verifiedId == null) {
        return;
    }

    String avatar = null;
    if (body != null) {
        avatar = body.get("avatar");
    }

    studentRepository.updateAvatar(verifiedId, avatar);
}
}
