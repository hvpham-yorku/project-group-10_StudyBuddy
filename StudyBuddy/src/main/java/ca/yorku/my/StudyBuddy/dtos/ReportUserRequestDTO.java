package ca.yorku.my.StudyBuddy.dtos;

public record ReportUserRequestDTO(
    String reportedUserId,
    String category,
    String details
) {}