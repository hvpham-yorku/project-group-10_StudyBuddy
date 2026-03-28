package ca.yorku.my.StudyBuddy.dtos;

import java.util.List;
import java.util.Map;

// This record allows the StudentController to receive updates to any field in the Student profile through an API call, based on what fields are included in the request
public record UpdateProfileRequestDTO(
        List<String> courses,
        List<String> studyVibes,
        Map<String, Boolean> privacySettings,
        String bio,
        String year,
        String program,
        String avatar,
        String location,
        Map<String, Double> exactLocation,
        Boolean twoFAEnabled,
        int autoTimeout,
        Boolean isOnline,
        Map<String, Boolean> notifications

) {}