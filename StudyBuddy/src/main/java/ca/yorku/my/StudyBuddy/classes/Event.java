package ca.yorku.my.StudyBuddy.classes;
import java.util.List;

// This class represents an event that students can create and join.
// NOTE: This complete matches the EventResponseDTO
// Despite this, Event.java is kept this way for architectural purposes

public class Event {
	private String id;
	private String title;
	private String course;
	private String host;
	private String location;
	private String date;
	private String time;
	private int duration;
	private String description;
	private int maxParticipants;
	private List<String> attendees;
	private List<String> tags;
	private String status;
	private List<Review> reviews;
	
	// Required for deserialization purposes with Firebase.
	public Event() {
		
	}
	
	public Event(String id, String title, String courses, String host, String location, String date, String time,
			int duration, String description, int maxParticipants, List<String> attendees, List<String> tags, String status,
			List<Review> reviews) {
		this.id = id;
		this.title = title;
		this.course = courses;
		this.host = host;
		this.location = location;
		this.date = date;
		this.time = time;
		this.duration = duration;
		this.description = description;
		this.maxParticipants = maxParticipants;
		this.attendees = attendees;
		this.tags = tags;
		this.status = status;
		this.reviews = reviews;
	}
	
	// Getters //
	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getCourse() {
		return course;
	}
	public String getHost() {
		return host;
	}
	public String getLocation() {
		return location;
	}
	public String getDate() {
		return date;
	}
	public String getTime() {
		return time;
	}
	public int getDuration() {
		return duration;
	}
	public String getDescription() {
		return description;
	}
	public int getMaxParticipants() {
		return maxParticipants;
	}
	public List<String> getAttendees() {
		return attendees;
	}
	public List<String> getTags() {
		return tags;
	}
	public String getStatus() {
		return status;
	}
	public List<Review> getReviews() {
		return reviews;
	}
	
	// Setters //
	public void setId(String id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setCourse(String courses) {
		this.course = courses;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setMaxParticipants(int maxParticipants) {
		this.maxParticipants = maxParticipants;
	}
	public void setAttendees(List<String> attendees) {
		this.attendees = attendees;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}
	
	
	
}