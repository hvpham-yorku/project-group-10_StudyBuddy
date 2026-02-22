package ca.yorku.my.StudyBuddy;

import java.util.List;
import java.util.Map;

public record UpdateProfileRequest(
        List<String> courses,
        List<String> studyVibes,
        Map<String, Boolean> privacySettings,
        String bio,
        String year,
        String program
) {}