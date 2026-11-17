
package com.unikart.product;

/*
 * Project Name: Unikart
 * Developer Name: Manansh
 * Development Date: 2026-11-17
 * Module: Product DAO Testing
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

public class ProductDAOTest extends TestCase {

    private ProductDAO productDAO;

    private int sellerId;
    private int anotherSellerId;
    private int categoryId;

    private final List<Integer> createdProductIds = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        productDAO = new ProductDAO();

        createTestSellers();
        findTestCategory();
    }

    private void createTestSellers() throws SQLException {

        sellerId = createSeller("Product Test Seller");
        anotherSellerId = createSeller("Another Test Seller");
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
                    "product_test_" + UUID.randomUUID() + "@chitkara.edu.in"
            );
            ps.setString(3, "TEST_ONLY_HASH");
            ps.setString(4, "SELLER");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                assertTrue("Seller ID should be generated.", rs.next());

                return rs.getInt(1);
            }
        }
    }

    private void findTestCategory() throws SQLException {

        String sql = "SELECT category_id FROM categories LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            assertTrue("At least one category must exist.", rs.next());

            categoryId = rs.getInt("category_id");
        }
    }

    private Product createTestProduct(int productSellerId)
            throws SQLException {

        String uniqueName = "Product Test " + UUID.randomUUID();

        Product product = new Product();

        product.setSellerId(productSellerId);
        product.setCategoryId(categoryId);
        product.setProductName(uniqueName);
        product.setDescription("Temporary product for DAO testing.");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(20);

        assertTrue(
                "Product should be inserted successfully.",
                productDAO.addProduct(product)
        );

        int productId = findProductId(uniqueName);

        assertTrue("Product ID should be generated.", productId > 0);

        createdProductIds.add(productId);

        return productDAO.getProductById(productId);
    }

    private int findProductId(String productName) throws SQLException {

        String sql =
                "SELECT product_id FROM products WHERE product_name = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, productName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("product_id");
                }
            }
        }

        return 0;
    }

    public void testAddProductStartsPending() throws SQLException {

        Product product = createTestProduct(sellerId);

        assertNotNull(product);
        assertEquals("PENDING", product.getStatus());
        assertEquals(Integer.valueOf(sellerId), product.getSellerId());
    }

    public void testApprovePendingProduct() throws SQLException {

        Product product = createTestProduct(sellerId);

        assertTrue(productDAO.approveProduct(product.getProductId()));

        Product updatedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(updatedProduct);
        assertEquals("APPROVED", updatedProduct.getStatus());
        assertNull(updatedProduct.getRejectionReason());
    }

    public void testRejectPendingProduct() throws SQLException {

        Product product = createTestProduct(sellerId);

        String reason = "Product description is incomplete.";

        assertTrue(
                productDAO.rejectProduct(product.getProductId(), reason)
        );

        Product updatedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(updatedProduct);
        assertEquals("REJECTED", updatedProduct.getStatus());
        assertEquals(reason, updatedProduct.getRejectionReason());
    }

    public void testMaterialUpdateReturnsProductToPending()
            throws SQLException {

        Product product = createTestProduct(sellerId);

        assertTrue(productDAO.approveProduct(product.getProductId()));

        product.setProductName(product.getProductName() + " Updated");
        product.setPrice(new BigDecimal("150.00"));

        assertTrue(productDAO.updateProduct(product, sellerId));

        Product updatedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(updatedProduct);
        assertEquals("PENDING", updatedProduct.getStatus());
        assertNull(updatedProduct.getRejectionReason());
        assertEquals(
                0,
                new BigDecimal("150.00").compareTo(updatedProduct.getPrice())
        );
    }

    public void testStockOnlyUpdatePreservesApproval()
            throws SQLException {

        Product product = createTestProduct(sellerId);

        assertTrue(productDAO.approveProduct(product.getProductId()));

        product.setStock(50);

        assertTrue(productDAO.updateProduct(product, sellerId));

        Product updatedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(updatedProduct);
        assertEquals("APPROVED", updatedProduct.getStatus());
        assertEquals(50, updatedProduct.getStock());
    }

    public void testSellerCannotUpdateAnotherSellersProduct()
            throws SQLException {

        Product product = createTestProduct(sellerId);

        String originalName = product.getProductName();

        product.setProductName(originalName + " Unauthorized");

        assertFalse(
                productDAO.updateProduct(product, anotherSellerId)
        );

        Product unchangedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(unchangedProduct);
        assertEquals(originalName, unchangedProduct.getProductName());
    }

    public void testSellerCannotDeleteAnotherSellersProduct()
            throws SQLException {

        Product product = createTestProduct(sellerId);

        assertFalse(
                productDAO.deleteProduct(
                        product.getProductId(),
                        anotherSellerId
                )
        );

        Product existingProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(existingProduct);
    }

    public void testSellerCannotResubmitAnotherSellersProduct()
            throws SQLException {

        Product product = createTestProduct(sellerId);

        assertTrue(
                productDAO.rejectProduct(
                        product.getProductId(),
                        "Test rejection reason"
                )
        );

        assertFalse(
                productDAO.resubmitProduct(
                        product.getProductId(),
                        anotherSellerId
                )
        );

        Product unchangedProduct =
                productDAO.getProductById(product.getProductId());

        assertNotNull(unchangedProduct);
        assertEquals("REJECTED", unchangedProduct.getStatus());
    }

    @Override
    protected void tearDown() throws Exception {

        try (Connection connection = DBConnection.getConnection()) {

            for (int productId : createdProductIds) {

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
