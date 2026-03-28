package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("stub")
// This class allows for the student information to be accessed and modified in the stub database
public class StubStudentRepository implements StudentRepository {

    // Retrieves a student from the stub database using ID, if they exist, otherwise throws an exception
    @Override
    public Student getStudent(String userId) throws Exception {
        return StubDatabase.STUDENTS.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    // Saves a student to the stub database using student ID
    @Override
    public void saveStudent(Student student) throws Exception {
        StubDatabase.STUDENTS.removeIf(s -> s.getUserId().equals(student.getUserId()));
        StubDatabase.STUDENTS.add(student);
    }

    // Updates the courses a student is currently enrolled in
    @Override
    public void updateCourses(String userId, List<String> courses) throws Exception {
        Student student = getStudent(userId);
        student.setCourses(courses);
    }

    // Updates the study vibe a student inputs in their profile
    @Override
    public void updateStudyVibes(String userId, List<String> studyVibes) throws Exception {
        Student student = getStudent(userId);
        student.setStudyVibes(studyVibes);
    }

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updateBio(String userId, String bio) throws Exception {
        Student student = getStudent(userId);
        student.setBio(bio);
    }

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updateProgram(String userId, String program) throws Exception {
        Student student = getStudent(userId);
        student.setProgram(program);
    }

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updateYear(String userId, String year) throws Exception {
        Student student = getStudent(userId);
        student.setYear(year);
    }

    // Updates the privacy settings of a student based on their choices
    @Override
    public void updatePrivacySettings(String userId, Map<String, Boolean> privacySettings) throws Exception {
        Student student = getStudent(userId);
        Map<String, Boolean> current = student.getPrivacySettings();
        current.putAll(privacySettings);
        student.setPrivacySettings(current);
    }

    // Updates the profile picture of a student based on their input
    @Override
    public void updateAvatar(String userId, String avatar) throws Exception {
        Student student = getStudent(userId);
        student.setAvatar(avatar);
    }

    // Updates the location of a student based on their input
    @Override
    public void updateNotifications(String userId, Map<String, Boolean> notifications) throws Exception {
        Student student = getStudent(userId);
        Map<String, Boolean> current = student.getNotifications();
        current.putAll(notifications);
        student.setNotifications(current);
    }

    // Updates the location of a student based on their input
    @Override
    public void updateLocation(String userId, String location) throws Exception {
        Student student = getStudent(userId);
        student.setLocation(location);
    }

    // Updates the online status of a student based on their input
    @Override
    public void updateOnlineStatus(String userId, Boolean isOnline) throws Exception {
        Student student = getStudent(userId);
        student.setIsOnline(isOnline);
    }

    // Updates the two-factor authentication setting of a student based on their input
    @Override
    public void updateTwoFAEnabled(String userId, Boolean twoFAEnabled) throws Exception {
        Student student = getStudent(userId);
        student.setTwoFAEnabled(twoFAEnabled);
    }

    // Updates the auto timeout duration of a student based on their input
    @Override
    public void updateAutoTimeout(String userId, int autoTimeout) throws Exception {
        Student student = getStudent(userId);
        student.setAutoTimeout(autoTimeout);
    }

	@Override
	public void updateAttendedEventIDs(String userId, List<String> attendedEventIds) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateExactLocation(String userId, Map<String, Double> exactLocation) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportUser(String reporterUserId, String reportedUserId, String category, String details)
			throws Exception {
        // ID-97 smell fix: reject invalid report payloads early in stub mode.
        if (reporterUserId == null || reporterUserId.isBlank()) {
            throw new IllegalArgumentException("Reporter user ID is required");
        }
        if (reportedUserId == null || reportedUserId.isBlank()) {
            throw new IllegalArgumentException("Reported user ID is required");
        }
        if (reporterUserId.equals(reportedUserId)) {
            throw new IllegalArgumentException("You cannot report yourself");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Report category is required");
        }

        // Validate both users exist before storing a report record.
        getStudent(reporterUserId);
        getStudent(reportedUserId);

        // Persist report in stub store so UI success reflects real state in this profile.
        StubDatabase.REPORTS.add(new StubDatabase.ReportRecord(
            reporterUserId,
            reportedUserId,
            category.trim(),
            details == null ? "" : details.trim(),
            System.currentTimeMillis()
        ));
	}
    
}
