package ca.yorku.my.StudyBuddy;

public class Student {
	
	private String firstName;
	private String lastName;
	
	private String[] courses;
	
	public Student(String firstName, String lastName, String[] courses) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.setCourses(courses);
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


	public String[] getCourses() {
		return courses;
	}

	public void setCourses(String[] courses) {
		this.courses = courses;
	}

}
