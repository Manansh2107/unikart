
package com.unikart.service;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 2026-11-17
 * Module: Product Service Testing
 */

import com.unikart.database.DBConnection;
import com.unikart.product.Product;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProductServiceTest extends TestCase {

    private ProductService productService;

    private int sellerId;
    private int anotherSellerId;
    private int categoryId;
    private int productId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        productService = new ProductService();

        sellerId = createSeller("Service Test Seller");
        anotherSellerId = createSeller("Another Service Seller");

        findCategory();
    }

    private int createSeller(String name) throws SQLException {

        String sql =
                "INSERT INTO users (name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(
                    2,
                    "service_test_" + UUID.randomUUID() + "@chitkara.edu.in"
            );
            ps.setString(3, "TEST_ONLY_HASH");
            ps.setString(4, "SELLER");

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

            assertTrue("A category must exist.", rs.next());

            categoryId = rs.getInt("category_id");
        }
    }

    private Product createProduct() {

        Product product = new Product();

        product.setSellerId(sellerId);
        product.setCategoryId(categoryId);
        product.setProductName("Service Test " + UUID.randomUUID());
        product.setDescription("Temporary service test product.");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(20);

        assertTrue(productService.addProduct(product));

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT product_id FROM products WHERE product_name = ?")) {

            ps.setString(1, product.getProductName());

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue("Product should exist in database.", rs.next());

                productId = rs.getInt("product_id");
                product.setProductId(productId);

                return product;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void testAddValidProduct() {

        Product product = createProduct();

        assertTrue(product.getProductId() > 0);

        Product savedProduct =
                productService.getProductById(productId);

        assertNotNull(savedProduct);
        assertEquals("PENDING", savedProduct.getStatus());
    }

    public void testAddProductWithInvalidPrice() {

        Product product = new Product();

        product.setSellerId(sellerId);
        product.setCategoryId(categoryId);
        product.setProductName("Invalid Price Product");
        product.setPrice(BigDecimal.ZERO);
        product.setStock(10);

        try {
            productService.addProduct(product);
            fail("Zero price should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Product price must be greater than zero!",
                    expected.getMessage()
            );
        }
    }

    public void testAddProductWithNegativeStock() {

        Product product = new Product();

        product.setSellerId(sellerId);
        product.setCategoryId(categoryId);
        product.setProductName("Negative Stock Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(-5);

        try {
            productService.addProduct(product);
            fail("Negative stock should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Product stock cannot be negative!",
                    expected.getMessage()
            );
        }
    }

    public void testAddProductWithInvalidSeller() {

        Product product = new Product();

        product.setSellerId(null);
        product.setCategoryId(categoryId);
        product.setProductName("Invalid Seller Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(10);

        try {
            productService.addProduct(product);
            fail("Null seller ID should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals("Invalid seller ID!", expected.getMessage());
        }
    }

    public void testSellerCannotUpdateAnotherSellersProduct() {

        Product product = createProduct();

        product.setProductName("Unauthorized Product Update");

        try {
            productService.updateProduct(product, anotherSellerId);
            fail("Another seller must not update this product.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "You cannot update another seller's product!",
                    expected.getMessage()
            );
        }
    }

    public void testSellerCannotDeleteAnotherSellersProduct() {

        createProduct();

        try {
            productService.deleteProduct(productId, anotherSellerId);
            fail("Another seller must not delete this product.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "You cannot delete another seller's product!",
                    expected.getMessage()
            );
        }

        assertNotNull(productService.getProductById(productId));
    }

    public void testRejectProductRequiresReason() {

        Product product = createProduct();

        try {
            productService.rejectProduct(product.getProductId(), " ");
            fail("Empty rejection reason should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Rejection reason cannot be empty!",
                    expected.getMessage()
            );
        }
    }

    public void testSearchRequiresKeyword() {

        try {
            productService.searchProducts(" ");
            fail("Empty search keyword should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Search keyword cannot be empty!",
                    expected.getMessage()
            );
        }
    }

    public void testSortRequiresValidOrder() {

        try {
            productService.sortProductsByPrice("INVALID");
            fail("Invalid sorting order should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "Order must be ASC or DESC!",
                    expected.getMessage()
            );
        }
    }

    public void testValidProductCanBeApproved() {

        Product product = createProduct();

        assertTrue(
                productService.approveProduct(product.getProductId())
        );

        Product approvedProduct =
                productService.getProductById(product.getProductId());

        assertEquals("APPROVED", approvedProduct.getStatus());
    }

    @Override
    protected void tearDown() throws Exception {

        try (Connection connection = DBConnection.getConnection()) {

            if (productId > 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM products WHERE product_id = ?")) {

                    ps.setInt(1, productId);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM users WHERE user_id IN (?, ?)")) {

                if (sellerId > 0 && anotherSellerId > 0) {
                    ps.setInt(1, sellerId);
                    ps.setInt(2, anotherSellerId);
                    ps.executeUpdate();
                }
            }

        } finally {
            super.tearDown();
        }
    }
}
