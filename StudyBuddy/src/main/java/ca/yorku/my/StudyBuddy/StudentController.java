package ca.yorku.my.StudyBuddy;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController 
@RequestMapping("/api/studentcontroller")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {
	
	@GetMapping("/getstudents")
	public ArrayList<Student> getAllStudents() {
		return StubDatabase.STUDENTS;
	}
}