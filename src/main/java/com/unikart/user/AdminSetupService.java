
package com.unikart.user;

import com.unikart.database.DBConnection;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 18 November 2026
 * Module: Initial Admin Account Setup
 */

public class AdminSetupService {

    private static final String ADMIN_LOCK =
            "unikart_first_admin_setup";

    private final UserDAO userDAO;

    public AdminSetupService() {
        this.userDAO = new UserDAO();
    }

    public static void main(String[] args) {

        Console console = System.console();

        if (console == null) {
            System.out.println(
                    "Please run this program directly from PowerShell."
            );
            return;
        }

        char[] password = null;
        char[] confirmPassword = null;

        try {
            System.out.println("===== UNIKART ADMIN SETUP =====");

            String name = console.readLine("Enter admin name: ");
            String email = console.readLine("Enter Chitkara email: ");
            String phone = console.readLine(
                    "Enter phone number (optional): "
            );

            password = console.readPassword("Enter admin password: ");
            confirmPassword = console.readPassword(
                    "Confirm admin password: "
            );

            if (password == null || confirmPassword == null) {
                throw new IllegalArgumentException(
                        "Password input was cancelled."
                );
            }

            if (!Arrays.equals(password, confirmPassword)) {
                throw new IllegalArgumentException(
                        "Passwords do not match."
                );
            }

            if (password.length < 8) {
                throw new IllegalArgumentException(
                        "Password must contain at least 8 characters."
                );
            }

            String passwordText = new String(password);

            new AdminSetupService().createFirstAdmin(
                    name,
                    email,
                    passwordText,
                    phone
            );

            passwordText = null;

        } catch (Exception e) {

            System.out.println(
                    "Admin setup failed: " + e.getMessage()
            );

        } finally {

            if (password != null) {
                Arrays.fill(password, '\0');
            }

            if (confirmPassword != null) {
                Arrays.fill(confirmPassword, '\0');
            }
        }
    }

    public void createFirstAdmin(
            String name,
            String email,
            String password,
            String phone) throws SQLException {

        // Validate name
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Name is required."
            );
        }

        name = name.trim();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Name cannot exceed 100 characters."
            );
        }

        // Validate college email
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.length() > 150
                || !email.matches(
                "^[A-Za-z0-9._%+-]+@chitkara\\.edu\\.in$")) {

            throw new IllegalArgumentException(
                    "Please enter a valid Chitkara email."
            );
        }

        // Validate password
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        // Validate optional phone number
        if (phone != null && !phone.isBlank()) {
            phone = phone.trim();

            if (!phone.matches("[0-9]{10}")) {
                throw new IllegalArgumentException(
                        "Phone number must contain exactly 10 digits."
                );
            }
        } else {
            phone = null;
        }

        // Acquire a database-level lock
        try (Connection connection = DBConnection.getConnection()) {

            boolean lockAcquired = false;

            try {
                String lockSQL = "SELECT GET_LOCK(?, 10)";

                try (PreparedStatement statement =
                             connection.prepareStatement(lockSQL)) {

                    statement.setString(1, ADMIN_LOCK);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()
                                && result.getInt(1) == 1) {
                            lockAcquired = true;
                        } else {
                            throw new SQLException(
                                    "Could not acquire admin setup lock."
                            );
                        }
                    }
                }

                // Check whether an admin already exists
                String countSQL =
                        "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";

                try (PreparedStatement statement =
                             connection.prepareStatement(countSQL);
                     ResultSet result = statement.executeQuery()) {

                    result.next();

                    if (result.getInt(1) > 0) {
                        throw new IllegalStateException(
                                "An admin account already exists."
                        );
                    }
                }

                // Check whether the email is already registered
                if (userDAO.findByEmail(email).isPresent()) {
                    throw new IllegalArgumentException(
                            "This email is already registered."
                    );
                }

                // Hash the password
                String passwordHash =
                        PasswordUtil.hashPassword(password);

                // Create an ADMIN user
                User admin = new User(
                        name,
                        email,
                        passwordHash,
                        phone,
                        User.Role.ADMIN
                );

                // Save the admin in the database
                int adminId = userDAO.createUser(admin);

                System.out.println(
                        "Admin account created successfully!"
                );

                System.out.println("Admin ID: " + adminId);
                System.out.println("Admin Email: " + email);
                System.out.println("Role: ADMIN");

            } finally {

                // Release the database lock
                if (lockAcquired) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         "SELECT RELEASE_LOCK(?)")) {

                        statement.setString(1, ADMIN_LOCK);
                        statement.executeQuery();
                    }
                }
            }
        }
    }
}
