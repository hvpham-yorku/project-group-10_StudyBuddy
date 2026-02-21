package ca.yorku.my.StudyBuddy;

import java.util.List;

public record StudentDTO (
	String userId,
	String email,
	String displayName,
	String firstName,
	String lastName,
	String program,
	String bio,
	String profilePic,
	List<String> courses,
	List<String> attendedEventIds
) {}