package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.github.wulkanowy.sdk.scrapper.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.*;

@RestController 
@RequestMapping("/api/studentcontroller")
@CrossOrigin(origins = "*")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private SessionLogService sessionLogService;
	
	@GetMapping("/getstudents")
	public ArrayList<Student> getAllStudents() {
		return StubDatabase.STUDENTS;
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
