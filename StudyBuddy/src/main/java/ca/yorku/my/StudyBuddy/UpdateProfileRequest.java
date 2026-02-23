package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

// This record allows the StudentController to receive updates to any field in the Student profile through an API call, based on what fields are included in the request
public record UpdateProfileRequest(
        List<String> courses,
        List<String> studyVibes,
        Map<String, Boolean> privacySettings,
        String bio,
        String year,
        String program
        
) {}