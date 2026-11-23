/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.unikart.database.DBConnection;

public class SellerOrderGroupDAO {

    // Create a seller group inside an existing order.
    // The connection is provided by the checkout transaction.
    public int createSellerOrderGroup(Connection connection,
                                      int orderId,
                                      Integer sellerId) throws SQLException {

        String sql = """
                INSERT INTO seller_order_groups
                (order_id, seller_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, orderId);

            if (sellerId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, sellerId);
            }

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        }

        throw new SQLException("Could not create seller order group.");
    }

    // Retrieve all seller groups belonging to a main order.
    public List<SellerOrderGroup> getGroupsByOrderId(int orderId) {

        List<SellerOrderGroup> groups = new ArrayList<>();

        String sql = """
                SELECT *
                FROM seller_order_groups
                WHERE order_id = ?
                ORDER BY group_id
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int sellerIdValue = resultSet.getInt("seller_id");

                    Integer sellerId = resultSet.wasNull()
                            ? null : sellerIdValue;

                    SellerOrderGroup group = new SellerOrderGroup(
                            resultSet.getInt("group_id"),
                            resultSet.getInt("order_id"),
                            sellerId,
                            resultSet.getString("group_status"),
                            resultSet.getTimestamp("created_at")
                    );

                    groups.add(group);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving seller order groups!");
            e.printStackTrace();
        }

        return groups;
    }

    // Update the status of a seller's order group.
    public boolean updateGroupStatus(int groupId, String status) {

        String sql = """
                UPDATE seller_order_groups
                SET group_status = ?
                WHERE group_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, groupId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating seller group status!");
            e.printStackTrace();
        }

        return false;
    }
}
