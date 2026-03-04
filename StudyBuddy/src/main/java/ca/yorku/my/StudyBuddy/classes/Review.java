package ca.yorku.my.StudyBuddy.classes;

import java.util.List;

// TODO: Make associated DTO; Currently sent raw from middle layer instead
// of being processed.
public class Review {
	private String id;
	private String author;
	private int rating;
	private String text;
	private String date;
	private List<Comment> comments;
	
	public Review() {
		
	}
	
	public Review(String id, String author, int rating, String text, String date, List<Comment> comments) {
		this.id = id;
		this.author = author;
		this.rating = rating;
		this.text = text;
		this.date = date;
		this.comments = comments;
	}
	
	public String getId() {
		return id;
	}
	public String getAuthor() {
		return author;
	}
	public int getRating() {
		return rating;
	}
	public String getText() {
		return text;
	}
	public String getDate() {
		return date;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
}
