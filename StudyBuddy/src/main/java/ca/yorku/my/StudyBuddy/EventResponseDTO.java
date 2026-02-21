package ca.yorku.my.StudyBuddy;

import java.util.List;

public record EventResponseDTO(
    String id,
    String title,
    String course,
    String location,
    String description,
    String status,
    String date,
    String time,
    int duration,
    int maxParticipants,
    List<String> attendees,
    List<String> tags,
    List<String> reviews,
    String hostId,
    String hostName,
    String hostAvatar
) {}