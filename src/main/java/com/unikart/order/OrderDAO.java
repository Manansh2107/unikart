
package com.unikart.order;

import com.unikart.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Save a new order and return its generated order ID
    public int createOrder(int customerId, BigDecimal totalAmount) {

        String sql = "INSERT INTO orders (customer_id, total_amount) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, customerId);
            statement.setBigDecimal(2, totalAmount);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error creating order!");
            e.printStackTrace();
        }

        return -1;
    }

    // Retrieve an order using its ID
    public Order getOrderById(int orderId) {

        String sql = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return new Order(
                            resultSet.getInt("order_id"),
                            resultSet.getInt("customer_id"),
                            resultSet.getBigDecimal("total_amount"),
                            resultSet.getString("order_status"),
                            resultSet.getTimestamp("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving order!");
            e.printStackTrace();
        }

        return null;
    }

    // Retrieve all orders belonging to a customer
    public List<Order> getOrdersByCustomerId(int customerId) {

        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    Order order = new Order(
                            resultSet.getInt("order_id"),
                            resultSet.getInt("customer_id"),
                            resultSet.getBigDecimal("total_amount"),
                            resultSet.getString("order_status"),
                            resultSet.getTimestamp("created_at")
                    );

                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving customer orders!");
            e.printStackTrace();
        }

        return orders;
    }

    // Update order status
    public boolean updateOrderStatus(int orderId, String status) {

        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, orderId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating order status!");
            e.printStackTrace();
        }

        return false;
    }
}
