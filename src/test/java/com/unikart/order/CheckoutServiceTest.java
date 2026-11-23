/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 2026-11-17
 * Module: Checkout Service Testing
 */

import com.unikart.database.DBConnection;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutServiceTest extends TestCase {

    private CheckoutService checkoutService;

    private int customerId;
    private int sellerOneId;
    private int sellerTwoId;
    private int cartId;
    private int categoryId;

    private final List<Integer> productIds = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        checkoutService = new CheckoutService();

        customerId = createUser("Checkout Test Customer", "CUSTOMER");
        sellerOneId = createUser("Checkout Seller One", "SELLER");
        sellerTwoId = createUser("Checkout Seller Two", "SELLER");

        findCategory();
        createCart();
    }

    private int createUser(String name, String role) throws SQLException {

        String sql =
                "INSERT INTO users (name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(
                    2,
                    "checkout_test_" + UUID.randomUUID() + "@chitkara.edu.in"
            );
            ps.setString(3, "TEST_ONLY_HASH");
            ps.setString(4, role);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue("User ID should be generated.", rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void findCategory() throws SQLException {

        String sql = "SELECT category_id FROM categories LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            assertTrue("At least one category must exist.", rs.next());

            categoryId = rs.getInt("category_id");
        }
    }

    private void createCart() throws SQLException {

        String sql = "INSERT INTO carts (user_id) VALUES (?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, customerId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue("Cart ID should be generated.", rs.next());
                cartId = rs.getInt(1);
            }
        }
    }

    private int createProduct(
            Integer sellerId,
            String status,
            int stock,
            String price
    ) throws SQLException {

        String sql =
                "INSERT INTO products " +
                "(seller_id, category_id, product_name, description, " +
                "price, stock, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (sellerId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, sellerId);
            }

            ps.setInt(2, categoryId);
            ps.setString(3, "Checkout Product " + UUID.randomUUID());
            ps.setString(4, "Temporary checkout test product.");
            ps.setBigDecimal(5, new BigDecimal(price));
            ps.setInt(6, stock);
            ps.setString(7, status);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue("Product ID should be generated.", rs.next());

                int id = rs.getInt(1);
                productIds.add(id);

                return id;
            }
        }
    }

    private void addCartItem(int productId, int quantity)
            throws SQLException {

        String sql =
                "INSERT INTO cart_items (cart_id, product_id, quantity) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);

            assertEquals(1, ps.executeUpdate());
        }
    }

    private int getStock(int productId) throws SQLException {

        String sql = "SELECT stock FROM products WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt("stock");
            }
        }
    }

    private int getCartItemCount() throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM cart_items WHERE cart_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, cartId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private int getOrderCount() throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM orders WHERE customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    public void testSuccessfulCheckout() throws SQLException {

        int productId = createProduct(
                sellerOneId, "APPROVED", 10, "50.00"
        );

        addCartItem(productId, 2);

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertTrue("Checkout should return a valid order ID.", orderId > 0);

        assertEquals(8, getStock(productId));
        assertEquals(0, getCartItemCount());
        assertEquals(1, getOrderCount());

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT total_amount FROM orders WHERE order_id = ?")) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());

                assertEquals(
                        0,
                        new BigDecimal("100.00").compareTo(
                                rs.getBigDecimal("total_amount")
                        )
                );
            }
        }
    }

    public void testPaymentRecordIsCreated() throws SQLException {

        int productId = createProduct(
                sellerOneId, "APPROVED", 10, "75.00"
        );

        addCartItem(productId, 1);

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertTrue(orderId > 0);

        String sql =
                "SELECT amount, payment_method, payment_status, " +
                "transaction_reference FROM payments WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("Payment record should exist.", rs.next());

                assertEquals(
                        0,
                        new BigDecimal("75.00").compareTo(
                                rs.getBigDecimal("amount")
                        )
                );

                assertEquals("TEST", rs.getString("payment_method"));
                assertEquals("SUCCESS", rs.getString("payment_status"));
                assertEquals(
                        "SIM-ORDER-" + orderId,
                        rs.getString("transaction_reference")
                );
            }
        }
    }

    public void testInsufficientStockPreventsCheckout()
            throws SQLException {

        int productId = createProduct(
                sellerOneId, "APPROVED", 5, "100.00"
        );

        addCartItem(productId, 6);

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertEquals(-1, orderId);
        assertEquals(5, getStock(productId));
        assertEquals(1, getCartItemCount());
        assertEquals(0, getOrderCount());
    }

    public void testUnapprovedProductCannotBePurchased()
            throws SQLException {

        int productId = createProduct(
                sellerOneId, "PENDING", 10, "100.00"
        );

        addCartItem(productId, 1);

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertEquals(-1, orderId);
        assertEquals(10, getStock(productId));
        assertEquals(1, getCartItemCount());
        assertEquals(0, getOrderCount());
    }

    public void testEmptyCartPreventsCheckout() throws SQLException {

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertEquals(-1, orderId);
        assertEquals(0, getOrderCount());
    }

    public void testMultipleSellersCreateSeparateGroups()
            throws SQLException {

        int productOne = createProduct(
                sellerOneId, "APPROVED", 10, "100.00"
        );

        int productTwo = createProduct(
                sellerTwoId, "APPROVED", 10, "200.00"
        );

        addCartItem(productOne, 1);
        addCartItem(productTwo, 2);

        int orderId = checkoutService.checkout(customerId, "TEST");

        assertTrue(orderId > 0);

        String groupSql =
                "SELECT COUNT(*) FROM seller_order_groups " +
                "WHERE order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(groupSql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
        }

        String itemSql =
                "SELECT COUNT(*) FROM order_items oi " +
                "JOIN seller_order_groups sog ON oi.group_id = sog.group_id " +
                "WHERE sog.order_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(itemSql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
        }

        assertEquals(9, getStock(productOne));
        assertEquals(8, getStock(productTwo));
        assertEquals(0, getCartItemCount());
    }

    @Override
    protected void tearDown() throws Exception {

        try (Connection connection = DBConnection.getConnection()) {

            // Find all orders created for this temporary customer.
            List<Integer> orderIds = new ArrayList<>();

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT order_id FROM orders WHERE customer_id = ?")) {

                ps.setInt(1, customerId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        orderIds.add(rs.getInt("order_id"));
                    }
                }
            }

            for (int orderId : orderIds) {

                // Delete payment records.
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM payments WHERE order_id = ?")) {

                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete order items belonging to seller groups.
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM order_items WHERE group_id IN " +
                        "(SELECT group_id FROM seller_order_groups " +
                        "WHERE order_id = ?)")) {

                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete seller order groups.
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM seller_order_groups WHERE order_id = ?")) {

                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Delete main order.
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM orders WHERE order_id = ?")) {

                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
            }

            // Delete temporary cart items and cart.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE cart_id = ?")) {

                if (cartId > 0) {
                    ps.setInt(1, cartId);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM carts WHERE cart_id = ?")) {

                if (cartId > 0) {
                    ps.setInt(1, cartId);
                    ps.executeUpdate();
                }
            }

            // Delete temporary products.
            for (int productId : productIds) {

                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM products WHERE product_id = ?")) {

                    ps.setInt(1, productId);
                    ps.executeUpdate();
                }
            }

            // Delete temporary users.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM users WHERE user_id IN (?, ?, ?)")) {

                ps.setInt(1, customerId);
                ps.setInt(2, sellerOneId);
                ps.setInt(3, sellerTwoId);

                ps.executeUpdate();
            }

        } finally {
            super.tearDown();
        }
    }
}
