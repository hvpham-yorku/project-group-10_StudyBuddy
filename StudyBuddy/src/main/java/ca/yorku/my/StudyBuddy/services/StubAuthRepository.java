package ca.yorku.my.StudyBuddy.services;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Student;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Profile("stub")
public class StubAuthRepository implements AuthRepository {

    @Override
    public String registerUser(String email, String password, String firstName, String lastName, String major, String year) {
        Student s = new Student(UUID.randomUUID().toString(), firstName, lastName);
        s.setEmail(email);
        s.setProgram(major);
        s.setYear(year);
        StubDatabase.STUDENTS.add(s);
        return s.getUserId();
    }

    @Override
    public String loginUser(String email, String password) {
        Student s = StubDatabase.STUDENTS.stream()
            .filter(st -> email.equals(st.getEmail()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        return s.getUserId();
    }

    @Override
    public String verifyFrontendToken(String authHeader) {
        return authHeader.replace("Bearer ", "");
    }

    @Override
    public String generateResetLink(String email) {
        return "http://localhost:5173/reset-mock";
    }

    @Override
    public void logoutUser(String authHeader) {
        // Stub environment: no external token store to revoke.
        // Simply treat request as successful.
    }

    @Override
    public String verifyTwoFA(String email, String code) throws Exception {
        // Stub environment: accept any 6-digit code and return the user's ID as token.
        Student s = StubDatabase.STUDENTS.stream()
            .filter(st -> email.equals(st.getEmail()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No account found for this email."));
        return s.getUserId();
    }
}