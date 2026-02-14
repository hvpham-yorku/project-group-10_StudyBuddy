package ca.yorku.my.StudyBuddy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.yorku.my.StudyBuddy.SessionLogService;

@WebMvcTest(StudentController.class)
class StudentTests {
	
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SessionLogService sessionLogService;

	@BeforeEach
	void setUp() {
		StubDatabase.STUDENTS.clear();
	}
	
	// ==================== EXISTING TESTS ====================

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
		String[] s1Courses = {"LE-EECS-2311-Z", "SC-PHYS-2020-M", "LE-EECS-4413-Z", "LE-EECS-3421-Z", "LE-EECS-2101-X"};
		StubDatabase.STUDENTS.add(new Student("1", "John", "Doe", s1Courses));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudents"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("John"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("Doe"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[0]").value("LE-EECS-2311-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[1]").value("SC-PHYS-2020-M"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[2]").value("LE-EECS-4413-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[3]").value("LE-EECS-3421-Z"))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].courses[4]").value("LE-EECS-2101-X"));
	}

	// ==================== 10 AI-GENERATED UNIT TESTS ====================

	/**
	 * Test 1: Student constructor with userId sets all fields correctly
	 */
	@Test
	void testStudentConstructorWithUserId() {
		String[] courses = {"EECS 2311", "EECS 3311"};
		Student s = new Student("stu-1", "Alice", "Smith", courses);

		assert s.getUserId().equals("stu-1");
		assert s.getFirstName().equals("Alice");
		assert s.getLastName().equals("Smith");
		assert s.getCourses().length == 2;
		assert s.getAttendedEventIds() != null;
		assert s.getAttendedEventIds().isEmpty();
	}

	/**
	 * Test 2: Student 3-arg constructor auto-generates userId
	 */
	@Test
	void testStudentConstructorAutoUserId() {
		String[] courses = {"MATH 1013"};
		Student s = new Student("Bob", "Jones", courses);

		assert s.getUserId().equals("Bob-Jones");
		assert s.getFirstName().equals("Bob");
		assert s.getLastName().equals("Jones");
		assert s.getAttendedEventIds().isEmpty();
	}

	/**
	 * Test 3: addAttendedEvent adds event and prevents duplicates
	 */
	@Test
	void testAddAttendedEventNoDuplicates() {
		Student s = new Student("1", "Jane", "Doe", new String[]{});

		s.addAttendedEvent("event-100");
		assert s.getAttendedEventIds().size() == 1;

		// Adding the same event again should not duplicate
		s.addAttendedEvent("event-100");
		assert s.getAttendedEventIds().size() == 1;

		// Adding a different event should work
		s.addAttendedEvent("event-200");
		assert s.getAttendedEventIds().size() == 2;
	}

	/**
	 * Test 4: Student getter/setter for profile fields (email, bio, program)
	 */
	@Test
	void testStudentProfileFields() {
		Student s = new Student();
		s.setEmail("test@my.yorku.ca");
		s.setBio("I love studying");
		s.setProgram("Computer Science");
		s.setFullName("Test User");
		s.setProfilePic("pic.jpg");

		assert s.getEmail().equals("test@my.yorku.ca");
		assert s.getBio().equals("I love studying");
		assert s.getProgram().equals("Computer Science");
		assert s.getFullName().equals("Test User");
		assert s.getProfilePic().equals("pic.jpg");
	}

	/**
	 * Test 5: StubDatabase can hold multiple students
	 */
	@Test
	void testStubDatabaseMultipleStudents() {
		StubDatabase.STUDENTS.add(new Student("1", "A", "B", new String[]{"EECS 2311"}));
		StubDatabase.STUDENTS.add(new Student("2", "C", "D", new String[]{"MATH 1013"}));
		StubDatabase.STUDENTS.add(new Student("3", "E", "F", new String[]{"PHYS 2020"}));

		assert StubDatabase.STUDENTS.size() == 3;
		assert StubDatabase.STUDENTS.get(1).getFirstName().equals("C");
		assert StubDatabase.STUDENTS.get(2).getUserId().equals("3");
	}

	/**
	 * Test 6: GET /getstudents returns empty list when no students exist
	 */
	@Test
	void testGetStudentsEmpty() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudents"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
	}

	/**
	 * Test 7: GET /getstudent/{id}/sessionlog returns empty when student not found
	 */
	@Test
	void testSessionLogStudentNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudent/nonexistent/sessionlog"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
	}

	/**
	 * Test 8: GET /getstudent/{id}/totalstudytime returns 0 when student not found
	 */
	@Test
	void testTotalStudyTimeStudentNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudent/nonexistent/totalstudytime"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("0"));
	}

	/**
	 * Test 9: GET /getstudent/{id}/sessionlog returns events for valid student
	 */
	@Test
	void testSessionLogReturnsEvents() throws Exception {
		Student s = new Student("stu-1", "Test", "User", new String[]{});
		s.addAttendedEvent("evt-1");
		StubDatabase.STUDENTS.add(s);

		// Mock the service to return a list with one event
		Event mockEvent = new Event("host1", "Study Group", "EECS 2311", "Library",
			"desc", "2026-02-20 14:00", "2026-02-20 16:00", 5);
		mockEvent.setEventId("evt-1");
		when(sessionLogService.getStudentSessionLog(anyList()))
			.thenReturn(Arrays.asList(mockEvent));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/studentcontroller/getstudent/stu-1/sessionlog"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].eventId").value("evt-1"))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].course").value("EECS 2311"));
	}

	/**
	 * Test 10: POST /addeventtosessionlog returns false for non-existent student
	 */
	@Test
	void testAddEventToSessionLogStudentNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post(
				"/api/studentcontroller/getstudent/nonexistent/addeventtosessionlog/evt-1"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("false"));
	}

}
