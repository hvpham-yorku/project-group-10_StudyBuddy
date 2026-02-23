package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class creates the Student object which contains all the information related to a student
public class Student {
	
	// Registration & Schema Fields
    private String firstName;
	private String lastName;
    private String userId;      
    private String email;       
    private String fullName;    
    private String program;     
    private String bio;         
    private String profilePic;  
    private String year;

	
	private List<String> courses = new ArrayList<>();
    private List<String> studyVibes = new ArrayList<>();
    private Map<String, Boolean> privacySettings = new HashMap<>();
	private List<String> attendedEventIds = new ArrayList<>();

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
    
 // teammate/original constructor
    public Student(String userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.courses = new ArrayList<>();
        this.studyVibes = new ArrayList<>();
        this.privacySettings = new HashMap<>();
        this.bio = "";
        this.program = "";
        this.year = "";
        this.profilePic = "";
    }


    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public List<String> getCourses() { return courses; }
    public void setCourses(List<String> courses) { this.courses = courses; }

    public List<String> getStudyVibes() { return studyVibes; }
    public void setStudyVibes(List<String> studyVibes) { this.studyVibes = studyVibes; }

    public Map<String, Boolean> getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(Map<String, Boolean> privacySettings) { this.privacySettings = privacySettings; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
