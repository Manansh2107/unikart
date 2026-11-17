
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Registers a new customer.
     * New registrations always receive the CUSTOMER role.
     */
    public User registerCustomer(
            String name,
            String email,
            String password,
            String phone) throws SQLException {

        // Validate input
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        name = name.trim();
        email = email.trim().toLowerCase();

        // Check whether the email is already registered
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        // Hash the password before storing it
        String passwordHash = PasswordUtil.hashPassword(password);

        // New users are always registered as customers
        User user = new User(
                name,
                email,
                passwordHash,
                phone,
                User.Role.CUSTOMER
        );

        // Save the user and get the generated database ID
        int userId = userDAO.createUser(user);

        // Return the saved user
        return userDAO.findById(userId)
                .orElseThrow(() -> new SQLException(
                        "User was created but could not be retrieved."
                ));
    }

    /**
     * Authenticates a user using their email and password.
     *
     * Returns the user if authentication succeeds.
     * Returns Optional.empty() if the credentials are incorrect.
     */
    public Optional<User> login(
            String email,
            String password) throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        email = email.trim().toLowerCase();

        // Find the account using email
        Optional<User> userOptional = userDAO.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        // Verify the entered password against the stored hash
        boolean passwordMatches = PasswordUtil.verifyPassword(
                password,
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
}

