package com.studybuddy.repository;

import com.studybuddy.model.Student;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StubStudentRepository {

    private final Map<String, Student> students = new HashMap<>();

    public StubStudentRepository() {
        Student s = new Student();
        s.setUserId("u1");
        s.setFirstName("Alex");
        s.setLastName("Johnson");
        s.setEmail("alex.johnson@my.yorku.ca");
        s.setProgram("Computer Science (EECS)");
        s.setYear("3rd Year");
        s.setBio("Passionate about algorithms and coffee. Always down for a good study session! 🎓");
        s.setAvatar("https://images.unsplash.com/photo-1758611971095-87f590f8c4ed?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx5b3VuZyUyMG1hbiUyMHN0dWRlbnQlMjBzdHVkeWluZyUyMGRlc2t8ZW58MXx8fHwxNzcxNDY4ODEyfDA&ixlib=rb-4.1.0&q=80&w=1080");

        s.setCourses(Arrays.asList("EECS 2311", "EECS 3311", "MATH 2030", "EECS 3000", "EECS 4080"));
        s.setStudyVibes(Arrays.asList("Group Discussion", "Whiteboard Work", "Problem Solving"));

        s.setTotalStudyHours(127);
        s.setTotalEventsAttended(34);

        s.setOnline(true);
        s.setLocation("Scott Library, Level 3");
        s.setJoinedDate("September 2024");

        // Privacy settings (matches frontend)
        Map<String, Boolean> privacy = new HashMap<>();
        privacy.put("showEmail", false);
        privacy.put("showLocation", true);
        privacy.put("showCourses", true);
        privacy.put("showStudyVibes", true);
        privacy.put("showSessionHistory", true);
        privacy.put("showProfilePic", true);
        privacy.put("showBio", true);
        s.setPrivacySettings(privacy);

        // Security
        s.setTwoFAEnabled(true);
        s.setAutoTimeout(15);

        // Notifications
        Map<String, Boolean> notifications = new HashMap<>();
        notifications.put("chatMessages", true);
        notifications.put("sessionUpdates", true);
        notifications.put("connectionRequests", true);
        s.setNotifications(notifications);

        students.put("u1", s);
    }

    // -----------------------------
    // Get student
    // -----------------------------
    public Student getStudent(String id) {
        return students.get(id);
    }

    // -----------------------------
    // Update profile fields
    // -----------------------------
    public void updateProfile(String id, String bio, String program, String year) {
        Student s = getStudent(id);
        if (s == null){
            return;
        }

        if (bio != null) {
            s.setBio(bio);
        }
        if (program != null){
            s.setProgram(program);
        }
        if (year != null){
            s.setYear(year);
        } 
    }

    // -----------------------------
    // Update avatar
    // -----------------------------
    public void updateAvatar(String id, String avatarUrl) {
        Student s = getStudent(id);
        if (s == null){
            return;
        }
        s.setAvatar(avatarUrl);
    }

    // -----------------------------
    // Update courses
    // -----------------------------
    public void updateCourses(String id, List<String> courses) {
        Student s = getStudent(id);
        if (s == null){
            return;
        }
        if (courses != null){
            s.setCourses(courses);
    }
}

    // -----------------------------
    // Update study vibes
    // -----------------------------
    public void updateStudyVibes(String id, List<String> vibes) {
        Student s = getStudent(id);
        if (s == null){
            return;
        }
        if (vibes != null){
            s.setStudyVibes(vibes);
        }
    }

    // -----------------------------
    // Update privacy settings (map merge)
    // -----------------------------
    public void updatePrivacySettings(String id, Map<String, Boolean> newSettings) {
        Student s = getStudent(id);
        if (s == null || newSettings == null) return;

        Map<String, Boolean> existing = s.getPrivacySettings();

        for (String key : newSettings.keySet()) {
            existing.put(key, newSettings.get(key));
        }

        s.setPrivacySettings(existing);
    }

    // -----------------------------
    // Update notifications (map merge)
    // -----------------------------
    public void updateNotifications(String id, Map<String, Boolean> newSettings) {
        Student s = getStudent(id);
        if (s == null || newSettings == null) return;

        Map<String, Boolean> existing = s.getNotifications();

        for (String key : newSettings.keySet()) {
            existing.put(key, newSettings.get(key));
        }

        s.setNotifications(existing);
    }
}