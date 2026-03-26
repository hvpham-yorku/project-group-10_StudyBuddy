package ca.yorku.my.StudyBuddy.classes;

public class UserReport {
    private String reportedUserId;
    private String reportedUserName;
    private String reportedByUserId;
    private String reportedByUserEmail;
    private String reason;
    private String createdAt;
    private String status;

    public UserReport() {}

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public String getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(String reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getReportedByUserEmail() {
        return reportedByUserEmail;
    }

    public void setReportedByUserEmail(String reportedByUserEmail) {
        this.reportedByUserEmail = reportedByUserEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}