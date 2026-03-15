package ca.yorku.my.StudyBuddy.dtos;

import java.util.List;

public record StudentSessionLogResponseDTO(
    StudentSessionLogSummaryDTO summary,
    List<StudentSessionLogEventDTO> events
) {}
