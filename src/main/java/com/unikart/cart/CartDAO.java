/*

 * Developer: MANANSH GOYAL
 * Development Date: 16-11-2026
 */
package com.unikart.cart;

import com.unikart.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class CartDAO {
   public Cart getCartByUserId(int userId) {

    String sql = "SELECT * FROM carts WHERE user_id = ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {

        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            int cartId = rs.getInt("cart_id");

            return new Cart(cartId, userId);
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return null;
} 
public Cart createCart(int userId) {

    String sql = "INSERT INTO carts (user_id) VALUES (?)";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(
                 sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

        ps.setInt(1, userId);

        int rowsInserted = ps.executeUpdate();

        if (rowsInserted > 0) {

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {

                int cartId = rs.getInt(1);

                return new Cart(cartId, userId);
            }
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return null;
}
public boolean addItemToCart(int cartId, int productId, int quantity) {

    if (quantity <= 0) {
        return false;
    }

    String checkSql = "SELECT stock, status FROM products WHERE product_id = ?";

    String existingSql =
            "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";

    String insertSql =
            "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + ?";

    try (Connection connection = DBConnection.getConnection()) {

        // Step 1: Check product stock and approval status
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {

            ps.setInt(1, productId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return false;
            }

            int stock = rs.getInt("stock");
            String status = rs.getString("status");

            if (!"APPROVED".equals(status)) {
                return false;
            }

            // Step 2: Check existing cart quantity
            int existingQuantity = 0;

            try (PreparedStatement check = connection.prepareStatement(existingSql)) {

                check.setInt(1, cartId);
                check.setInt(2, productId);

                ResultSet existing = check.executeQuery();

                if (existing.next()) {
                    existingQuantity = existing.getInt("quantity");
                }
            }

            if (existingQuantity + quantity > stock) {
                return false;
            }
        }

        // Step 3: Insert item or increase existing quantity
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);

            return ps.executeUpdate() > 0;
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return false;
}
public List<CartItem> getCartItems(int cartId) {

    List<CartItem> items = new ArrayList<>();

    String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, " +
                 "p.product_name, p.price, ci.quantity " +
                 "FROM cart_items ci " +
                 "JOIN products p ON ci.product_id = p.product_id " +
                 "WHERE ci.cart_id = ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {

        ps.setInt(1, cartId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                CartItem item = new CartItem(
                        rs.getInt("cart_item_id"),
                        rs.getInt("cart_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                );

                items.add(item);
            }
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return items;
}

public boolean updateCartItemQuantity(int cartId, int productId, int quantity) {

    if (quantity <= 0) {
        return false;
    }

    String checkSql =
            "SELECT stock, status FROM products WHERE product_id = ?";

    String updateSql =
            "UPDATE cart_items SET quantity = ? " +
            "WHERE cart_id = ? AND product_id = ?";

    try (Connection connection = DBConnection.getConnection()) {

        // Step 1: Check product stock and approval status
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return false;
                }

                int stock = rs.getInt("stock");
                String status = rs.getString("status");

                if (!"APPROVED".equals(status) || quantity > stock) {
                    return false;
                }
            }
        }

        // Step 2: Update cart quantity
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.setInt(3, productId);

            return ps.executeUpdate() > 0;
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return false;
}

public boolean removeItemFromCart(int cartId, int productId) {

    String sql =
            "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {

        ps.setInt(1, cartId);
        ps.setInt(2, productId);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return false;
}

public boolean clearCart(int cartId) {

    String sql = "DELETE FROM cart_items WHERE cart_id = ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {

        ps.setInt(1, cartId);

        ps.executeUpdate();

        return true;

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return false;
}

public double getCartTotal(int cartId) {

    String sql =
            "SELECT SUM(p.price * ci.quantity) AS total " +
            "FROM cart_items ci " +
            "JOIN products p ON ci.product_id = p.product_id " +
            "WHERE ci.cart_id = ?";

    try (Connection connection = DBConnection.getConnection();
         PreparedStatement ps = connection.prepareStatement(sql)) {

        ps.setInt(1, cartId);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }

    } catch (SQLException e) {

        e.printStackTrace();
    }

    return 0.0;
}

}
