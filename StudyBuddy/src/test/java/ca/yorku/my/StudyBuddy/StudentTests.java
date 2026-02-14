package ca.yorku.my.StudyBuddy;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ca.yorku.my.StudyBuddy.SessionLogService;

@WebMvcTest(StudentController.class)
class StudentTests {
	
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessionLogService sessionLogService;
	
	// Test if StubDatabase is working
	// Reference for feature unit tests
	@Test
	void unitTest1() throws Exception {
    	String[] s1Courses = {"LE-EECS-2311-Z", "SC-PHYS-2020-M", "LE-EECS-4413-Z", "LE-EECS-3421-Z", "LE-EECS-2101-X"};
		StubDatabase.STUDENTS.add(new Student("1", "John", "Doe", s1Courses));
		
		// Now access it from the database
		
		Student student = new Student(StubDatabase.STUDENTS.get(0).getFirstName(),
				StubDatabase.STUDENTS.get(0).getLastName(),
				StubDatabase.STUDENTS.get(0).getCourses());
		
		// Test the outputs
		assert(student.getFirstName() == "John");
		assert(student.getLastName() == "Doe");
		assert(student.getCourses().length == 5);
		}
	
	// Test if API is working
	@Test
	void apiTest1() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudents"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        // Response should look something like this:
        // [
        // 	{	"firstName":"John",
        //		"lastName":"Doe",
        //		"courses": ["LE-EECS-2311-Z",
        // 					"SC-PHYS-2020-M",
        //					"LE-EECS-4413-Z",
        //					"LE-EECS-3421-Z",
        //					"LE-EECS-2101-X"]
        //  }
        // ]
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("John"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("Doe"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[0]").value("LE-EECS-2311-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[1]").value("SC-PHYS-2020-M"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[2]").value("LE-EECS-4413-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[3]").value("LE-EECS-3421-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[4]").value("LE-EECS-2101-X"));
	}

}
