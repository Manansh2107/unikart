
package com.unikart.service;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 18 November 2026
 * Module: Product Service Testing
 */

import com.unikart.database.DBConnection;
import com.unikart.product.Product;
import com.unikart.user.User;
import com.unikart.user.UserDAO;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductServiceTest extends TestCase {

    private ProductService productService;
    private UserDAO userDAO;

    private User sellerUser;
    private User anotherSellerUser;
    private User adminUser;

    private int categoryId;

    private final List<Integer> productIds = new ArrayList<>();
    private final List<Integer> userIds = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        productService = new ProductService();
        userDAO = new UserDAO();

        sellerUser = createUser("Service Test Seller", User.Role.SELLER);
        anotherSellerUser = createUser("Another Service Seller", User.Role.SELLER);
        adminUser = createUser("Service Test Admin", User.Role.ADMIN);

        findCategory();
    }

    // Creates a real test user in the database.
    private User createUser(String name, User.Role role) throws Exception {

        String email = "test_" + UUID.randomUUID() + "@unikart.test";

        String sql = """
                INSERT INTO users
                (name, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                """;

        int userId;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, "TEST_ONLY_HASH");
            statement.setString(4, role.name());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new Exception("Test user ID was not generated.");
                }

                userId = keys.getInt(1);
            }
        }

        userIds.add(userId);

        return userDAO.findById(userId)
                .orElseThrow(() -> new Exception("Test user not found."));
    }

    // Finds an existing category for product testing.
    private void findCategory() throws Exception {

        String sql = "SELECT category_id FROM categories LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                categoryId = resultSet.getInt("category_id");
            } else {
                throw new Exception("No product category found in database.");
            }
        }
    }

    // Creates a valid product using the authenticated seller.
    private Product createProduct() throws Exception {

        Product product = new Product();

        String productName = "Service Test Product " + UUID.randomUUID();

        product.setCategoryId(categoryId);
        product.setProductName(productName);
        product.setDescription("Product created for service testing");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(20);

        assertTrue(
                productService.addSellerProduct(sellerUser, product)
        );

        int productId = findProductId(productName);

        productIds.add(productId);
        product.setProductId(productId);

        return product;
    }

    // Finds a product ID using its unique name.
    private int findProductId(String productName) throws Exception {

        String sql = """
                SELECT product_id
                FROM products
                WHERE product_name = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productName);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("product_id");
                }
            }
        }

        throw new Exception("Created product was not found.");
    }

    // Test 1: A seller can add a valid product.
    public void testAddValidProduct() throws Exception {

        Product product = createProduct();

        Product savedProduct =
                productService.getProductById(product.getProductId());

        assertNotNull(savedProduct);
        assertEquals("PENDING", savedProduct.getStatus());
        assertEquals(
                sellerUser.getUserId(),
                savedProduct.getSellerId().intValue()
        );
    }

    // Test 2: An admin can add a Unikart-owned product.
    public void testAdminCanAddUnikartProduct() throws Exception {

        Product product = new Product();

        String productName = "Unikart Test Product " + UUID.randomUUID();

        product.setCategoryId(categoryId);
        product.setProductName(productName);
        product.setDescription("Product owned by Unikart");
        product.setPrice(new BigDecimal("250.00"));
        product.setStock(15);

        assertTrue(productService.addAdminProduct(adminUser, product));

        int productId = findProductId(productName);
        productIds.add(productId);

        Product savedProduct = productService.getProductById(productId);

        assertNotNull(savedProduct);
        assertNull(savedProduct.getSellerId());
        assertEquals("PENDING", savedProduct.getStatus());
    }

    // Test 3: A product with an invalid price must be rejected.
    public void testAddProductWithInvalidPrice() throws Exception {

        Product product = new Product();

        product.setCategoryId(categoryId);
        product.setProductName("Invalid Price Product");
        product.setDescription("Testing invalid price");
        product.setPrice(BigDecimal.ZERO);
        product.setStock(10);

        try {
            productService.addSellerProduct(sellerUser, product);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertEquals(
                    "Product price must be greater than zero!",
                    exception.getMessage()
            );
        }
    }

    // Test 4: Negative stock must be rejected.
    public void testAddProductWithNegativeStock() throws Exception {

        Product product = new Product();

        product.setCategoryId(categoryId);
        product.setProductName("Negative Stock Product");
        product.setDescription("Testing negative stock");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(-5);

        try {
            productService.addSellerProduct(sellerUser, product);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertEquals(
                    "Product stock cannot be negative!",
                    exception.getMessage()
            );
        }
    }

    // Test 5: An admin cannot use the seller product creation method.
    public void testAdminCannotUseSellerProductMethod() throws Exception {

        Product product = new Product();

        product.setCategoryId(categoryId);
        product.setProductName("Unauthorized Seller Product");
        product.setDescription("Testing role authorization");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);

        try {
            productService.addSellerProduct(adminUser, product);
            fail("Expected SecurityException");
        } catch (SecurityException exception) {
            // Correct: an admin cannot use the seller-only method.
        }
    }

    // Test 6: A seller cannot update another seller's product.
    public void testSellerCannotUpdateAnotherSellersProduct()
            throws Exception {

        Product product = createProduct();

        product.setProductName("Unauthorized Updated Product");

        try {
            productService.updateProduct(anotherSellerUser, product);
            fail("Expected SecurityException");
        } catch (SecurityException exception) {
            // Correct: seller ownership is checked.
        }
    }

    // Test 7: A seller cannot delete another seller's product.
    public void testSellerCannotDeleteAnotherSellersProduct()
            throws Exception {

        Product product = createProduct();

        try {
            productService.deleteProduct(
                    anotherSellerUser,
                    product.getProductId()
            );

            fail("Expected SecurityException");

        } catch (SecurityException exception) {
            // Correct: seller ownership is checked.
        }

        assertNotNull(
                productService.getProductById(product.getProductId())
        );
    }

    // Test 8: A seller cannot approve a product.
    public void testSellerCannotApproveProduct() throws Exception {

        Product product = createProduct();

        try {
            productService.approveProduct(
                    sellerUser,
                    product.getProductId()
            );

            fail("Expected SecurityException");

        } catch (SecurityException exception) {
            // Correct: approval is an admin-only operation.
        }
    }

    // Test 9: An admin cannot reject a product without a reason.
    public void testRejectProductRequiresReason() throws Exception {

        Product product = createProduct();

        try {
            productService.rejectProduct(
                    adminUser,
                    product.getProductId(),
                    " "
            );

            fail("Expected IllegalArgumentException");

        } catch (IllegalArgumentException exception) {
            // Correct: rejection requires a reason.
        }
    }

    // Test 10: An admin can approve a pending product.
    public void testValidProductCanBeApproved() throws Exception {

        Product product = createProduct();

        assertTrue(
                productService.approveProduct(
                        adminUser,
                        product.getProductId()
                )
        );

        Product approvedProduct =
                productService.getProductById(product.getProductId());

        assertEquals("APPROVED", approvedProduct.getStatus());
    }

    // Test 11: Empty search keywords must be rejected.
    public void testSearchWithEmptyKeyword() throws Exception {

        try {
            productService.searchProducts(" ");
            fail("Expected IllegalArgumentException");

        } catch (IllegalArgumentException exception) {
            // Correct: empty search is not allowed.
        }
    }

    // Test 12: Invalid sorting options must be rejected.
    public void testInvalidSortOption() throws Exception {

        try {
            productService.sortProductsByPrice("INVALID_SORT");
            fail("Expected IllegalArgumentException");

        } catch (IllegalArgumentException exception) {
            // Correct: unsupported sorting option is rejected.
        }
    }

    @Override
    protected void tearDown() throws Exception {

        // Delete test products first because they reference users.
        try (Connection connection = DBConnection.getConnection()) {

            for (int productId : productIds) {

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM products WHERE product_id = ?")) {

                    statement.setInt(1, productId);
                    statement.executeUpdate();
                }
            }

            // Delete test users after their products are removed.
            for (int userId : userIds) {

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     "DELETE FROM users WHERE user_id = ?")) {

                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
            }
        }

        super.tearDown();
    }
}
