/*
 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;

public class UserManagementService {

    private final UserDAO userDAO;

    public UserManagementService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Retrieves a user's profile.
     */
    public Optional<User> getUserProfile(
            User authenticatedUser) throws SQLException {

        validateAuthenticatedUser(authenticatedUser);

        return userDAO.findById(authenticatedUser.getUserId());
    }

    /**
     * Updates the profile of the currently authenticated user.
     */
    public boolean updateProfile(
            User authenticatedUser,
            String name,
            String phone) throws SQLException {

        validateAuthenticatedUser(authenticatedUser);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name cannot be empty."
            );
        }

        return userDAO.updateUserDetails(
                authenticatedUser.getUserId(),
                name.trim(),
                phone == null || phone.isBlank()
                        ? null : phone.trim()
        );
    }

    /**
     * Validates the authenticated user.
     */
    private void validateAuthenticatedUser(User user) {

        if (user == null || user.getUserId() <= 0
                || user.getRole() == null) {

            throw new SecurityException(
                    "A valid authenticated user is required."
            );
        }
    }
}
