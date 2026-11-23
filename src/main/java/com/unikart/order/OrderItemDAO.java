/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    // Save an item inside a seller order group.
    // The Connection is supplied by the calling transaction.
    public boolean createOrderItem(Connection connection,
                                   int groupId,
                                   int productId,
                                   String productName,
                                   int quantity,
                                   BigDecimal unitPrice) throws SQLException {

        String sql = """
                INSERT INTO order_items
                (group_id, product_id, product_name,
                 quantity, unit_price, subtotal)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        BigDecimal subtotal = unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);
            statement.setInt(2, productId);
            statement.setString(3, productName);
            statement.setInt(4, quantity);
            statement.setBigDecimal(5, unitPrice);
            statement.setBigDecimal(6, subtotal);

            return statement.executeUpdate() > 0;
        }
    }

    // Retrieve all items belonging to a seller order group.
    public List<OrderItem> getItemsByGroupId(int groupId) {

        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM order_items
                WHERE group_id = ?
                ORDER BY order_item_id
                """;

        try (Connection connection =
                     com.unikart.database.DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, groupId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    OrderItem item = new OrderItem(
                            resultSet.getInt("order_item_id"),
                            resultSet.getInt("group_id"),
                            resultSet.getInt("product_id"),
                            resultSet.getString("product_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price"),
                            resultSet.getBigDecimal("subtotal")
                    );

                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving order items!");
            e.printStackTrace();
        }

        return items;
    }
}
