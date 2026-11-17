
package com.unikart.order;

import com.unikart.database.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class PaymentDAO {

    // Create a payment record inside an existing database transaction.
    public int createPayment(Connection connection,
                             int orderId,
                             BigDecimal amount,
                             String paymentMethod,
                             String paymentStatus,
                             String transactionReference)
            throws SQLException {

        String sql = """
                INSERT INTO payments
                (order_id, amount, payment_method,
                 payment_status, transaction_reference)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, orderId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, paymentMethod);
            statement.setString(4, paymentStatus);
            statement.setString(5, transactionReference);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        }

        throw new SQLException("Could not create payment record.");
    }

    // Retrieve payment details using order ID.
    public Payment getPaymentByOrderId(int orderId) {

        String sql = """
                SELECT *
                FROM payments
                WHERE order_id = ?
                ORDER BY payment_id DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return new Payment(
                            resultSet.getInt("payment_id"),
                            resultSet.getInt("order_id"),
                            resultSet.getBigDecimal("amount"),
                            resultSet.getString("payment_method"),
                            resultSet.getString("payment_status"),
                            resultSet.getString("transaction_reference"),
                            resultSet.getTimestamp("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving payment details!");
            e.printStackTrace();
        }

        return null;
    }

    // Update payment status.
    public boolean updatePaymentStatus(int paymentId, String status) {

        String sql = """
                UPDATE payments
                SET payment_status = ?
                WHERE payment_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, paymentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating payment status!");
            e.printStackTrace();
        }

        return false;
    }
}
