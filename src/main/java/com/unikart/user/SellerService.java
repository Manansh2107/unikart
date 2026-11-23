/*

 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.sql.SQLException;
import java.util.List;

public class SellerService {

    private final SellerApplicationDAO applicationDAO;

    public SellerService() {
        this.applicationDAO = new SellerApplicationDAO();
    }

    /**
     * Allows an authenticated CUSTOMER to submit a seller application.
     */
    public int applyForSeller(
            User authenticatedUser,
            String collegeName,
            String reason) throws SQLException {

        validateAuthenticatedUser(authenticatedUser);

        if (authenticatedUser.getRole() != User.Role.CUSTOMER) {
            throw new SecurityException(
                    "Only customers can apply for seller status."
            );
        }

        if (collegeName == null || collegeName.isBlank()) {
            throw new IllegalArgumentException(
                    "College name is required."
            );
        }

        collegeName = collegeName.trim();

        // Prevent multiple pending applications from the same user.
        List<SellerApplication> pendingApplications =
                applicationDAO.findPendingApplications();

        boolean alreadyApplied = pendingApplications.stream()
                .anyMatch(application ->
                        application.getUserId()
                                == authenticatedUser.getUserId()
                );

        if (alreadyApplied) {
            throw new IllegalArgumentException(
                    "You already have a pending seller application."
            );
        }

        SellerApplication application = new SellerApplication(
                authenticatedUser.getUserId(),
                collegeName,
                reason == null ? null : reason.trim()
        );

        return applicationDAO.createApplication(application);
    }

    /**
     * Allows an authenticated ADMIN to view pending applications.
     */
    public List<SellerApplication> getPendingApplications(
            User authenticatedAdmin) throws SQLException {

        requireAdmin(authenticatedAdmin);

        return applicationDAO.findPendingApplications();
    }

    /**
     * Allows an authenticated ADMIN to approve an application.
     */
    public boolean approveApplication(
            User authenticatedAdmin,
            int applicationId) throws SQLException {

        requireAdmin(authenticatedAdmin);

        if (applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid application ID."
            );
        }

        return applicationDAO.approveApplication(applicationId);
    }

    /**
     * Allows an authenticated ADMIN to reject an application.
     */
    public boolean rejectApplication(
            User authenticatedAdmin,
            int applicationId,
            String rejectionReason) throws SQLException {

        requireAdmin(authenticatedAdmin);

        if (applicationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid application ID."
            );
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException(
                    "A rejection reason is required."
            );
        }

        return applicationDAO.rejectApplication(
                applicationId,
                rejectionReason.trim()
        );
    }

    /**
     * Checks that a user object is available.
     */
    private void validateAuthenticatedUser(User user) {

        if (user == null || user.getUserId() <= 0
                || user.getRole() == null) {

            throw new SecurityException(
                    "A valid authenticated user is required."
            );
        }
    }

    /**
     * Ensures that only an ADMIN can perform admin operations.
     */
    private void requireAdmin(User authenticatedAdmin) {

        validateAuthenticatedUser(authenticatedAdmin);

        if (authenticatedAdmin.getRole() != User.Role.ADMIN) {
            throw new SecurityException(
                    "Access denied. Only admins can perform this action."
            );
        }
    }
}
