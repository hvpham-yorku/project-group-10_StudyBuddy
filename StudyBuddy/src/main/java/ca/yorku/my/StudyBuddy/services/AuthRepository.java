package ca.yorku.my.StudyBuddy.services;

public interface AuthRepository {
    String registerUser(String email, String password, String firstName, String lastName, String major, String year) throws Exception;
    String loginUser(String email, String password) throws Exception;
    String verifyFrontendToken(String authHeader) throws Exception;
    String generateResetLink(String email) throws Exception;
    void logoutUser(String authHeader) throws Exception;
    String verifyTwoFA(String email, String code) throws Exception;
}