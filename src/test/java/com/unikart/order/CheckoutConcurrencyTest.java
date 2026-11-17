package com.unikart.order;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 2026-11-17
 * Module: Checkout Concurrency Testing
 */

import com.unikart.database.DBConnection;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CheckoutConcurrencyTest extends TestCase {

    private CheckoutService checkoutService;

    private int customerOneId;
    private int customerTwoId;
    private int sellerId;

    private int cartOneId;
    private int cartTwoId;

    private int categoryId;
    private int productId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        checkoutService = new CheckoutService();

        customerOneId = createUser("Concurrency Customer One");
        customerTwoId = createUser("Concurrency Customer Two");
        sellerId = createUser("Concurrency Test Seller");

        findCategory();

        cartOneId = createCart(customerOneId);
        cartTwoId = createCart(customerTwoId);

        // Only ONE product is available.
        productId = createProduct();

        // Both customers try to purchase that same product.
        addCartItem(cartOneId);
        addCartItem(cartTwoId);
    }

    private int createUser(String name) throws SQLException {

        String sql =
                "INSERT INTO users (name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(
                    2,
                    "concurrency_" + UUID.randomUUID() + "@chitkara.edu.in"
            );
            ps.setString(3, "TEST_ONLY_HASH");
            ps.setString(
                    4,
                    name.contains("Seller") ? "SELLER" : "CUSTOMER"
            );

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void findCategory() throws SQLException {

        String sql = "SELECT category_id FROM categories LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            assertTrue("Category must exist.", rs.next());

            categoryId = rs.getInt("category_id");
        }
    }

    private int createCart(int userId) throws SQLException {

        String sql =
                "INSERT INTO carts (user_id) VALUES (?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private int createProduct() throws SQLException {

        String sql =
                "INSERT INTO products " +
                "(seller_id, category_id, product_name, description, " +
                "price, stock, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sellerId);
            ps.setInt(2, categoryId);
            ps.setString(
                    3,
                    "Concurrency Test Product " + UUID.randomUUID()
            );
            ps.setString(4, "Temporary product for concurrency testing.");
            ps.setBigDecimal(5, new java.math.BigDecimal("100.00"));
            ps.setInt(6, 1);
            ps.setString(7, "APPROVED");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void addCartItem(int cartId) throws SQLException {

        String sql =
                "INSERT INTO cart_items (cart_id, product_id, quantity) " +
                "VALUES (?, ?, 1)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);

            assertEquals(1, ps.executeUpdate());
        }
    }

    private int getStock() throws SQLException {

        String sql =
                "SELECT stock FROM products WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt("stock");
            }
        }
    }

    private int getCartItemCount(int cartId) throws SQLException {

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
                "SELECT COUNT(*) FROM orders " +
                "WHERE customer_id IN (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, customerOneId);
            ps.setInt(2, customerTwoId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private int getPaymentCount() throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM payments p " +
                "JOIN orders o ON p.order_id = o.order_id " +
                "WHERE o.customer_id IN (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, customerOneId);
            ps.setInt(2, customerTwoId);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    public void testTwoCustomersCannotPurchaseLastProduct()
            throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {

            // Task for customer one.
            Callable<Integer> customerOneCheckout = () -> {

                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Checkout start signal timed out."
                    );
                }

                return checkoutService.checkout(
                        customerOneId, "TEST"
                );
            };

            // Task for customer two.
            Callable<Integer> customerTwoCheckout = () -> {

                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Checkout start signal timed out."
                    );
                }

                return checkoutService.checkout(
                        customerTwoId, "TEST"
                );
            };

            Future<Integer> futureOne =
                    executor.submit(customerOneCheckout);

            Future<Integer> futureTwo =
                    executor.submit(customerTwoCheckout);

            // Wait until both threads are ready.
            assertTrue(
                    "Both checkout threads should be ready.",
                    ready.await(10, TimeUnit.SECONDS)
            );

            System.out.println(
                    "Both customers are ready to checkout!"
            );

            // Release both threads together.
            start.countDown();

            int resultOne =
                    futureOne.get(20, TimeUnit.SECONDS);

            int resultTwo =
                    futureTwo.get(20, TimeUnit.SECONDS);

            System.out.println("Customer One result: " + resultOne);
            System.out.println("Customer Two result: " + resultTwo);

            // Exactly one checkout must succeed.
            int successfulCheckouts = 0;

            if (resultOne > 0) {
                successfulCheckouts++;
            }

            if (resultTwo > 0) {
                successfulCheckouts++;
            }

            assertEquals(
                    "Exactly one customer should purchase the last item.",
                    1,
                    successfulCheckouts
            );

            // The losing checkout must return -1.
            assertTrue(
                    "One checkout must fail.",
                    resultOne == -1 || resultTwo == -1
            );

            // Stock must never become negative.
            assertEquals(
                    "The last product should be sold exactly once.",
                    0,
                    getStock()
            );

            // Only one order and payment should exist.
            assertEquals(1, getOrderCount());
            assertEquals(1, getPaymentCount());

            // The winner's cart should be empty.
            // The losing customer's cart should retain the item.
            int cartOneItems = getCartItemCount(cartOneId);
            int cartTwoItems = getCartItemCount(cartTwoId);

            assertTrue(
                    "Exactly one cart should retain its product.",
                    (cartOneItems == 0 && cartTwoItems == 1) ||
                    (cartOneItems == 1 && cartTwoItems == 0)
            );

            System.out.println(
                    "CONCURRENCY TEST PASSED!"
            );

        } finally {

            // Always release waiting tasks.
            start.countDown();

            // Stop the thread pool.
            executor.shutdownNow();

            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void tearDown() throws Exception {

        try (Connection connection = DBConnection.getConnection()) {

            // Find orders created by either temporary customer.
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT order_id FROM orders " +
                    "WHERE customer_id IN (?, ?)")) {

                ps.setInt(1, customerOneId);
                ps.setInt(2, customerTwoId);

                java.util.List<Integer> orderIds =
                        new java.util.ArrayList<>();

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        orderIds.add(rs.getInt("order_id"));
                    }
                }

                for (int orderId : orderIds) {

                    try (PreparedStatement delete =
                                 connection.prepareStatement(
                                         "DELETE FROM payments " +
                                         "WHERE order_id = ?")) {

                        delete.setInt(1, orderId);
                        delete.executeUpdate();
                    }

                    try (PreparedStatement delete =
                                 connection.prepareStatement(
                                         "DELETE FROM order_items " +
                                         "WHERE group_id IN " +
                                         "(SELECT group_id " +
                                         "FROM seller_order_groups " +
                                         "WHERE order_id = ?)")) {

                        delete.setInt(1, orderId);
                        delete.executeUpdate();
                    }

                    try (PreparedStatement delete =
                                 connection.prepareStatement(
                                         "DELETE FROM seller_order_groups " +
                                         "WHERE order_id = ?")) {

                        delete.setInt(1, orderId);
                        delete.executeUpdate();
                    }

                    try (PreparedStatement delete =
                                 connection.prepareStatement(
                                         "DELETE FROM orders " +
                                         "WHERE order_id = ?")) {

                        delete.setInt(1, orderId);
                        delete.executeUpdate();
                    }
                }
            }

            // Delete temporary cart items.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM cart_items WHERE cart_id IN (?, ?)")) {

                ps.setInt(1, cartOneId);
                ps.setInt(2, cartTwoId);
                ps.executeUpdate();
            }

            // Delete temporary carts.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM carts WHERE cart_id IN (?, ?)")) {

                ps.setInt(1, cartOneId);
                ps.setInt(2, cartTwoId);
                ps.executeUpdate();
            }

            // Delete temporary product.
            if (productId > 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM products WHERE product_id = ?")) {

                    ps.setInt(1, productId);
                    ps.executeUpdate();
                }
            }

            // Delete temporary users.
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM users WHERE user_id IN (?, ?, ?)")) {

                ps.setInt(1, customerOneId);
                ps.setInt(2, customerTwoId);
                ps.setInt(3, sellerId);

                ps.executeUpdate();
            }

        } finally {
            super.tearDown();
        }
    }
}
