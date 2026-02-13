package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

public class Student {
	
	private String userId;
	private String firstName;
	private String lastName;
	
	private String[] courses;
	private List<String> attendedEventIds;
	
	public Student(String userId, String firstName, String lastName, String[] courses) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.setCourses(courses);
		this.attendedEventIds = new ArrayList<>();
	}

	// Backwards-compatible constructor used by older code/tests
	public Student(String firstName, String lastName, String[] courses) {
		// generate a simple userId for compatibility; real IDs come from auth in production
		this(firstName + "-" + lastName, firstName, lastName, courses);
	}
	
	// Setters and getters for all student fields
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

    // Registration & Schema Fields
    private String userId;      
    private String email;       
    private String fullName;    
    private String program;     
    private String bio;         
    private String profilePic;  

    // 1. MUST HAVE: The empty constructor for Firebase/Spring automation
    public Student() {}

	public void setCourses(String[] courses) {
		this.courses = courses;
	}
	
	public List<String> getAttendedEventIds() {
		return attendedEventIds;
	}
	
	public void setAttendedEventIds(List<String> attendedEventIds) {
		this.attendedEventIds = attendedEventIds;
	}
	
	public void addAttendedEvent(String eventId) {
		if (!this.attendedEventIds.contains(eventId)) {
			this.attendedEventIds.add(eventId);
		}
	}
    // 2. TEAMMATE'S ORIGINAL: Keeps their old code from breaking
    public Student(String firstName, String lastName, String[] courses) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.courses = courses;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // Other schema fields (left for the Profile pair)
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    // original methods
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String[] getCourses() { return courses; }
    public void setCourses(String[] courses) { this.courses = courses; }
}