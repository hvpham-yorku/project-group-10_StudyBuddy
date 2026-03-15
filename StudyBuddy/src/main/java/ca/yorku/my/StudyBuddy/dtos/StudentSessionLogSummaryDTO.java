package ca.yorku.my.StudyBuddy.dtos;

public record StudentSessionLogSummaryDTO(
    int totalMinutes,
    int totalEvents,
    int hostedCount,
    int attendedCount
) {}
