package ca.yorku.my.StudyBuddy;

public class Student {
    // Teammate's Original Fields
    private String firstName;
    private String lastName;
    private String[] courses;

    // Registration & Schema Fields
    private String userId;      
    private String email;       
    private String fullName;    
    private String program;     
    private String bio;         
    private String profilePic;  

    // 1. MUST HAVE: The empty constructor for Firebase/Spring automation
    public Student() {}

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