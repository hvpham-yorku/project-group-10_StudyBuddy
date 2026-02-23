package ca.yorku.my.StudyBuddy;

// TODO: Make associated DTO; Currently sent raw from middle layer instead
// of being processed.
public class Reviews {
	private int id;
	private String author;
	private int rating;
	private String text;
	private String date;
	private Comment[] comments;
}
