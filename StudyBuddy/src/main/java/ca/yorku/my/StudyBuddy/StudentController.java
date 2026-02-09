package ca.yorku.my.StudyBuddy;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController 
@RequestMapping("/api/studentcontroller")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {
	
	@GetMapping("/getstudents")
	public ArrayList<Student> getAllStudents() {
		return StubDatabase.STUDENTS;
	}
}