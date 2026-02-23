package classes;

import java.util.ArrayList;
import java.util.List;

public class Student {
	
	private String firstName;
	private String lastName;
	
	// Registration & Schema Fields
    private String userId;      
    private String email;       
    private String fullName;    
    private String program;     
    private String bio;         
    private String profilePic;  
	
	private List<String> courses;
	private List<String> attendedEventIds;

    // 1. MUST HAVE: The empty constructor for Firebase/Spring automation
    public Student() {}
	
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
	
	public Student(String userId, String email, String fullName, String firstName,
			String lastName, String program, String bio, String profilePic,
			List<String> courses, List<String> attendedEventIds) {
		this.userId = userId;
		this.email = email;
		this.fullName = fullName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.program = program;
		this.bio = bio;
		this.profilePic = profilePic;
		this.courses = courses;
		this.attendedEventIds = attendedEventIds;
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
    public List<String> getCourses() { return courses; }
    public void setCourses(List<String> courses) { this.courses = courses; }
}