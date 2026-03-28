package ca.yorku.my.StudyBuddy.dtos;

public record StudentSessionLogEventDTO(
    String id,
    String title,
    String course,
    String location,
    String date,
    String time,
    int duration,
    String status,
    String role
) {}
