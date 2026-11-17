
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;

public class PasswordChangeService {

    private final UserDAO userDAO;

    public PasswordChangeService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Changes the password after verifying the current password.
     */
    public boolean changePassword(
            User authenticatedUser,
            String currentPassword,
            String newPassword) throws SQLException {

        if (authenticatedUser == null
                || authenticatedUser.getUserId() <= 0) {
            throw new SecurityException(
                    "A valid authenticated user is required."
            );
        }

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException(
                    "Current password is required."
            );
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters."
            );
        }

        Optional<User> userOptional =
                userDAO.findById(authenticatedUser.getUserId());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException(
                    "User account not found."
            );
        }

        User user = userOptional.get();

        if (!PasswordUtil.verifyPassword(
                currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        String newPasswordHash =
                PasswordUtil.hashPassword(newPassword);

        return userDAO.updatePasswordHash(
                user.getUserId(),
                newPasswordHash
        );
    }
}
