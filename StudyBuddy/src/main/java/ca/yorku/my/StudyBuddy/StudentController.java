package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import classes.Student;
import dtos.StudentDTO;
import services.StudentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.*;

@RestController 
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
	
	@Autowired
	private StudentService studentService;
	
	@GetMapping("/{studentId}")
	public ResponseEntity<StudentDTO> getStudentById(@PathVariable String studentId) {
		try {
			Student student = studentService.getStudentById(studentId);
			StudentDTO studentDTO = new StudentDTO(
					student.getUserId(),
					student.getEmail(),
					student.getFullName(),
					student.getFirstName(),
					student.getLastName(),
					student.getProgram(),
					student.getBio(),
					student.getProfilePic(),
					student.getCourses(),
					student.getAttendedEventIds()
					);
			
			return ResponseEntity.ok(studentDTO);
			
		} catch (Exception e) {
			System.out.println("Error occured during firebase conncetion");
			return null;
		}
	}
	
	@GetMapping
	public ResponseEntity<List<StudentDTO>> getAllStudents() {
		try {
			// 1. Get raw Students from Firestore
			List<Student> students = studentService.getAllStudents();
			
			// 2. Empty list to hold formatted DTOs
			List<StudentDTO> studentDTOs = new ArrayList<>();
			
			// 3. Loop through each student
			for (Student student : students) {
				StudentDTO studentDTO = new StudentDTO(
						student.getUserId(),
						student.getEmail(),
						student.getFullName(),
						student.getFirstName(),
						student.getLastName(),
						student.getProgram(),
						student.getBio(),
						student.getProfilePic(),
						student.getCourses(),
						student.getAttendedEventIds()
						);
				studentDTOs.add(studentDTO);
			}
			
			return ResponseEntity.ok(studentDTOs);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PostMapping
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentDTO studentDTO) {
        try {
        	// 1. Create new event; This is where DTO comes in handy so as to filter out any other values
        	Student student = new Student(
        		"", // Let this be filled out by Firebase
        		studentDTO.email(),
        		studentDTO.displayName(),
        		studentDTO.firstName(),
        		studentDTO.lastName(),
        		studentDTO.program(),
        		studentDTO.bio(),
        		studentDTO.profilePic(),
        		studentDTO.courses(),
        		studentDTO.attendedEventIds()
        			);
        	
        	// 2. Store the event in firebase + generated the id
        	studentService.createStudent(student);
        		
        	// 3. Print it back to user to indicate success
        	return ResponseEntity.status(HttpStatus.CREATED).body(studentDTO);

        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	
}
