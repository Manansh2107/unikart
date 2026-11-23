/*
 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.time.LocalDateTime;

public class SellerApplication {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    private int applicationId;
    private int userId;
    private String collegeName;
    private String reason;
    private Status status;
    private String rejectionReason;
    private LocalDateTime appliedAt;

    public SellerApplication() {
    }

    public SellerApplication(
            int userId,
            String collegeName,
            String reason) {

        this.userId = userId;
        this.collegeName = collegeName;
        this.reason = reason;
        this.status = Status.PENDING;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public String toString() {
        return "SellerApplication{" +
                "applicationId=" + applicationId +
                ", userId=" + userId +
                ", collegeName='" + collegeName + '\'' +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", appliedAt=" + appliedAt +
                '}';
    }
}
