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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.unikart.database.DBConnection;

public class SellerApplicationDAO {

    /**
     * Submits a seller application.
     * The application starts with PENDING status.
     */
    public int createApplication(
            SellerApplication application) throws SQLException {

        String sql = """
                INSERT INTO seller_applications
                (user_id, college_name, reason, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, application.getUserId());
            statement.setString(2, application.getCollegeName());
            statement.setString(3, application.getReason());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Seller application submission failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            throw new SQLException(
                    "Seller application created but no ID was returned."
            );
        }
    }

    /**
     * Finds a seller application by its ID.
     */
    public Optional<SellerApplication> findById(
            int applicationId) throws SQLException {

        String sql = """
                SELECT application_id, user_id, college_name,
                       reason, status, rejection_reason, applied_at
                FROM seller_applications
                WHERE application_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, applicationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapApplication(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns all applications currently waiting for admin review.
     */
    public List<SellerApplication> findPendingApplications()
            throws SQLException {

        String sql = """
                SELECT application_id, user_id, college_name,
                       reason, status, rejection_reason, applied_at
                FROM seller_applications
                WHERE status = 'PENDING'
                ORDER BY applied_at ASC
                """;

        List<SellerApplication> applications = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                applications.add(mapApplication(resultSet));
            }
        }

        return applications;
    }

    /**
     * Approves a pending application and changes the user's role
     * to SELLER in the same database transaction.
     *
     * Returns false if the application does not exist,
     * is not pending, or its user is not a CUSTOMER.
     */
    public boolean approveApplication(
            int applicationId) throws SQLException {

        String selectSql = """
                SELECT user_id, status
                FROM seller_applications
                WHERE application_id = ?
                FOR UPDATE
                """;

        String updateUserSql = """
                UPDATE users
                SET role = 'SELLER'
                WHERE user_id = ?
                  AND role = 'CUSTOMER'
                """;

        String updateApplicationSql = """
                UPDATE seller_applications
                SET status = 'APPROVED',
                    rejection_reason = NULL
                WHERE application_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int userId;

                // Lock the application row while processing it.
                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setInt(1, applicationId);

                    try (ResultSet resultSet = statement.executeQuery()) {

                        if (!resultSet.next()
                                || !"PENDING".equals(
                                        resultSet.getString("status"))) {

                            connection.rollback();
                            return false;
                        }

                        userId = resultSet.getInt("user_id");
                    }
                }

                // Promote only an existing CUSTOMER.
                try (PreparedStatement statement =
                             connection.prepareStatement(updateUserSql)) {

                    statement.setInt(1, userId);

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                // Mark the application as approved.
                try (PreparedStatement statement =
                             connection.prepareStatement(updateApplicationSql)) {

                    statement.setInt(1, applicationId);

                    if (statement.executeUpdate() != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Rejects a pending seller application.
     */
    public boolean rejectApplication(
            int applicationId,
            String rejectionReason) throws SQLException {

        String sql = """
                UPDATE seller_applications
                SET status = 'REJECTED',
                    rejection_reason = ?
                WHERE application_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, rejectionReason);
            statement.setInt(2, applicationId);

            return statement.executeUpdate() == 1;
        }
    }

    /**
     * Converts a database row into a SellerApplication object.
     */
    private SellerApplication mapApplication(ResultSet resultSet)
            throws SQLException {

        SellerApplication application = new SellerApplication();

        application.setApplicationId(
                resultSet.getInt("application_id")
        );

        application.setUserId(
                resultSet.getInt("user_id")
        );

        application.setCollegeName(
                resultSet.getString("college_name")
        );

        application.setReason(
                resultSet.getString("reason")
        );

        application.setStatus(
                SellerApplication.Status.valueOf(
                        resultSet.getString("status")
                )
        );

        application.setRejectionReason(
                resultSet.getString("rejection_reason")
        );

        Timestamp timestamp = resultSet.getTimestamp("applied_at");

        if (timestamp != null) {
            LocalDateTime appliedAt = timestamp.toLocalDateTime();
            application.setAppliedAt(appliedAt);
        }

        return application;
    }
}
