package dtos;

import java.util.List;

import classes.Reviews;

// These fields are required from the frontend.
// None of this information is confidential except maybe
// participants. If someone bans a person, then we need to send a warning
// that a banned user is joining this session as well.
public record EventResponseDTO(
	String id,
	String title,
	String courses,
	String host,
	String location,
	String date,
	String time,
	int duration,
	String description,
	int maxParticipants,
	String[] attendees,
	String[] tags,
	String status,
	Reviews[] reviews
) {}