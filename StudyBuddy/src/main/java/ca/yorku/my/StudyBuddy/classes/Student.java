package ca.yorku.my.StudyBuddy.classes;

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
    private String avatar;  
    private String year;
    private int totalStudyHours;
    private int totalEventsAttended;
    private Boolean isOnline;
    private String joinedDate;
    private String location;
    private ExactLocation exactLocation;
    private boolean twoFAEnabled;
    private int autoTimeout;
    private int loginStreak;
    private String lastLoginDate;
    
	private List<String> courses = new ArrayList<>();
    private List<String> studyVibes = new ArrayList<>();
    private Map<String, Boolean> privacySettings = new HashMap<>();
	private List<String> attendedEventIds = new ArrayList<>();
    private Map<String, Boolean> notifications = new HashMap<>();

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
        this.fullName = firstName + " " + lastName;
        this.courses = new ArrayList<>();
        this.studyVibes = new ArrayList<>();
        this.privacySettings = new HashMap<>();
        this.bio = "";
        this.program = "";
        this.year = "";
        this.avatar = "";
        privacySettings.put("showBio", true);
        privacySettings.put("showProgram", true);
        privacySettings.put("showYear", true);
        privacySettings.put("showEmail", true);
        privacySettings.put("showCourses", true);
        privacySettings.put("showStudyVibes", true);
        privacySettings.put("showProfilePic", true);
        privacySettings.put("showSessionHistory", true);
        privacySettings.put("showLocation", true);

        notifications.put("chatMessages", true);
        notifications.put("sessionUpdates", true);
        notifications.put("connectionRequests", true);

        twoFAEnabled = true;
        autoTimeout = 15;
        isOnline = true;
    }



    // Getters and Setters for fields
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() {
    String fn = firstName == null ? "" : firstName.trim();
    String ln = lastName == null ? "" : lastName.trim();

    if (fn.isEmpty() && ln.isEmpty()) {
        return "";
    }
    if (fn.isEmpty()) {
        return ln;
    }
    if (ln.isEmpty()) {
        return fn;
    }
    return fn + " " + ln;
    }
    
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public List<String> getCourses() { return courses; }
    public void setCourses(List<String> courses) {
    this.courses = (courses == null) ? new ArrayList<>() : courses;
    }

    public List<String> getStudyVibes() { return studyVibes; }
    public void setStudyVibes(List<String> studyVibes) { this.studyVibes = studyVibes; }

    public Map<String, Boolean> getPrivacySettings() { return privacySettings; }
    public void setPrivacySettings(Map<String, Boolean> privacySettings) { this.privacySettings = privacySettings; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public int getTotalStudyHours() { return totalStudyHours; }
    public void setTotalStudyHours(int totalStudyHours) { this.totalStudyHours = totalStudyHours; }

    public int getTotalEventsAttended() { return totalEventsAttended; }
    public void setTotalEventsAttended(int totalEventsAttended) { this.totalEventsAttended = totalEventsAttended; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ExactLocation getExactLocation() { return exactLocation; }
    public void setExactLocation(ExactLocation exactLocation) { this.exactLocation = exactLocation; }

    public boolean isTwoFAEnabled() { return twoFAEnabled; }
    public void setTwoFAEnabled(boolean twoFAEnabled) { this.twoFAEnabled = twoFAEnabled; }

    public int getAutoTimeout() { return autoTimeout; }
    public void setAutoTimeout(int autoTimeout) { this.autoTimeout = autoTimeout;}

    public Map<String, Boolean> getNotifications() { return notifications; }
    public void setNotifications(Map<String, Boolean> notifications) { this.notifications = notifications; }

    public int getLoginStreak() { return loginStreak; }
    public void setLoginStreak(int loginStreak) { this.loginStreak = loginStreak; }

    public String getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public static class ExactLocation {
        private double latitude;
        private double longitude;

        public ExactLocation() {}

        public ExactLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }

    
}