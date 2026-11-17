
package com.unikart.cart;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 2026-11-17
 * Module: Cart Testing
 */

import com.unikart.database.DBConnection;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CartTest extends TestCase {

    private CartDAO cartDAO;
    private int userId;
    private int cartId;
    private int productId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cartDAO = new CartDAO();

        createTestCustomer();
        createTestProduct();

        Cart cart = cartDAO.createCart(userId);

        assertNotNull("Test cart should be created.", cart);

        cartId = cart.getCartId();
    }

    private void createTestCustomer() throws SQLException {

        String sql =
                "INSERT INTO users (name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, "Cart Test Customer");
            ps.setString(
                    2,
                    "cart_test_" + UUID.randomUUID() + "@chitkara.edu.in"
            );
            ps.setString(3, "TEST_ONLY_HASH");
            ps.setString(4, "CUSTOMER");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue("Customer ID should be generated.", rs.next());
                userId = rs.getInt(1);
            }
        }
    }

    private void createTestProduct() throws SQLException {

        String categorySql =
                "SELECT category_id FROM categories LIMIT 1";

        int categoryId;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(categorySql);
             ResultSet rs = ps.executeQuery()) {

            assertTrue("At least one category must exist.", rs.next());

            categoryId = rs.getInt("category_id");
        }

        String productSql =
                "INSERT INTO products " +
                "(category_id, product_name, description, price, stock, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     productSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, categoryId);
            ps.setString(2, "Cart Test Notebook");
            ps.setString(3, "Temporary product for automated testing.");
            ps.setBigDecimal(4, new java.math.BigDecimal("50.00"));
            ps.setInt(5, 20);
            ps.setString(6, "APPROVED");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue("Product ID should be generated.", rs.next());
                productId = rs.getInt(1);
            }
        }
    }

    public void testGetCartByUserId() {

        Cart cart = cartDAO.getCartByUserId(userId);

        assertNotNull(cart);
        assertEquals(cartId, cart.getCartId());
        assertEquals(userId, cart.getUserId());
    }

    public void testAddItemToCart() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));

        List<CartItem> items = cartDAO.getCartItems(cartId);

        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(productId, items.get(0).getProductId());
    }

    public void testAddSameProductIncreasesQuantity() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));
        assertTrue(cartDAO.addItemToCart(cartId, productId, 3));

        List<CartItem> items = cartDAO.getCartItems(cartId);

        assertEquals(1, items.size());
        assertEquals(5, items.get(0).getQuantity());
    }

    public void testGetCartTotal() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));

        assertEquals(
                100.0,
                cartDAO.getCartTotal(cartId),
                0.01
        );
    }

    public void testUpdateCartItemQuantity() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));

        assertTrue(
                cartDAO.updateCartItemQuantity(cartId, productId, 5)
        );

        List<CartItem> items = cartDAO.getCartItems(cartId);

        assertEquals(5, items.get(0).getQuantity());
    }

    public void testRemoveItemFromCart() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));

        assertTrue(
                cartDAO.removeItemFromCart(cartId, productId)
        );

        assertTrue(cartDAO.getCartItems(cartId).isEmpty());
    }

    public void testClearCart() {

        assertTrue(cartDAO.addItemToCart(cartId, productId, 2));

        assertTrue(cartDAO.clearCart(cartId));

        assertTrue(cartDAO.getCartItems(cartId).isEmpty());

        assertEquals(0.0, cartDAO.getCartTotal(cartId), 0.01);
    }

    public void testInvalidQuantityIsRejected() {

        assertFalse(cartDAO.addItemToCart(cartId, productId, 0));
        assertFalse(cartDAO.addItemToCart(cartId, productId, -1));

        assertTrue(cartDAO.getCartItems(cartId).isEmpty());
    }

    @Override
    protected void tearDown() throws Exception {

        try (Connection connection = DBConnection.getConnection()) {

            // Remove cart items first.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE cart_id = ?")) {

                if (cartId > 0) {
                    ps.setInt(1, cartId);
                    ps.executeUpdate();
                }
            }

            // Remove the test cart.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM carts WHERE cart_id = ?")) {

                if (cartId > 0) {
                    ps.setInt(1, cartId);
                    ps.executeUpdate();
                }
            }

            // Remove the test product.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM products WHERE product_id = ?")) {

                if (productId > 0) {
                    ps.setInt(1, productId);
                    ps.executeUpdate();
                }
            }

            // Remove the test customer.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM users WHERE user_id = ?")) {

                if (userId > 0) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

        } finally {
            super.tearDown();
        }
    }
}
