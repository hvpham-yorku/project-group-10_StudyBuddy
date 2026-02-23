package classes;
import java.util.ArrayList;
import java.util.List;

// This class represents an event that students can create and join.
// NOTE: This complete matches the EventResponseDTO
// Despite this, Event.java is kept this way for architectural purposes

public class Event{
	private String id;
	private String title;
	private String courses;
	private String host;
	private String location;
	private String date;
	private String time;
	private int duration;
	private String description;
	private int maxParticipants;
	private String[] attendees;
	private String[] tags;
	private String status;
	private Reviews[] reviews;
	
	// Getters //
	public String getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getCourses() {
		return courses;
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
	public String[] getAttendees() {
		return attendees;
	}
	public String[] getTags() {
		return tags;
	}
	public String getStatus() {
		return status;
	}
	public Reviews[] getReviews() {
		return reviews;
	}
	
	// Setters //
	public void setId(String id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setCourses(String courses) {
		this.courses = courses;
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
	public void setAttendees(String[] attendees) {
		this.attendees = attendees;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setReviews(Reviews[] reviews) {
		this.reviews = reviews;
	}
	
	
	
}