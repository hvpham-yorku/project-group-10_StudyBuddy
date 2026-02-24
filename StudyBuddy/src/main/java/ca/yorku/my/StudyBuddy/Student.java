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
    private boolean showBio = true;
    private boolean showCourses = true;
    private boolean showStudyVibes = true;
    private boolean showProgram = true;
    private boolean showYear = true;
    private boolean showEmail = true;
	
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
        privacySettings.put("showBio", true);
        privacySettings.put("showProgram", true);
        privacySettings.put("showYear", true);
        privacySettings.put("showEmail", true);
        privacySettings.put("showCourses", true);
        privacySettings.put("showStudyVibes", true);
        privacySettings.put("showProfilePic", true);
        privacySettings.put("showSessionHistory", true);
        privacySettings.put("showLocation", true);
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

    public boolean isShowBio() { return showBio; }
    public void setShowBio(boolean showBio) { this.showBio = showBio; }

    public boolean isShowCourses() { return showCourses; }
    public void setShowCourses(boolean showCourses) { this.showCourses = showCourses; }

    public boolean isShowStudyVibes() { return showStudyVibes; }
    public void setShowStudyVibes(boolean showStudyVibes) { this.showStudyVibes = showStudyVibes; }

    public boolean isShowProgram() { return showProgram; }
    public void setShowProgram(boolean showProgram) { this.showProgram = showProgram; }

    public boolean isShowYear() { return showYear; }
    public void setShowYear(boolean showYear) { this.showYear = showYear; }

    public boolean isShowEmail() { return showEmail; }
    public void setShowEmail(boolean showEmail) { this.showEmail = showEmail; }
}
