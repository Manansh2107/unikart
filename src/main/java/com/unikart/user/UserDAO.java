/*
 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.unikart.database.DBConnection;

public class UserDAO {

    /**
     * Saves a new user in the database.
     * The password must already be securely hashed before calling this method.
     *
     * @return the generated user ID
     */
    public int createUser(User user) throws SQLException {

        String sql = """
                INSERT INTO users
                (name, email, password_hash, phone, role)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new SQLException("Creating user failed: no ID returned.");
        }
    }

    /**
     * Finds a user by their email address.
     */
    public Optional<User> findByEmail(String email) throws SQLException {

        String sql = """
                SELECT user_id, name, email, password_hash,
                       phone, role, created_at
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Finds a user by their database ID.
     */
    public Optional<User> findById(int userId) throws SQLException {

        String sql = """
                SELECT user_id, name, email, password_hash,
                       phone, role, created_at
                FROM users
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Updates a user's name and phone number.
     */
    public boolean updateUserDetails(
            int userId, String name, String phone) throws SQLException {

        String sql = """
                UPDATE users
                SET name = ?, phone = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, phone);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Maps a database row to a User object.
     */
    private User mapUser(ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setUserId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setPhone(resultSet.getString("phone"));

        user.setRole(
                User.Role.valueOf(resultSet.getString("role"))
        );

        Timestamp timestamp = resultSet.getTimestamp("created_at");

        if (timestamp != null) {
            LocalDateTime createdAt = timestamp.toLocalDateTime();
            user.setCreatedAt(createdAt);
        }

        return user;
    }

    /**
     * Updates a user's password hash.
     */
    public boolean updatePasswordHash(
            int userId, String passwordHash) throws SQLException {

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setInt(2, userId);

            return statement.executeUpdate() == 1;
        }
    }

}
