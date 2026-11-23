/*

 * Developers: Himanshu
 * Development Dates: 17-11-2026 to 18-11-2026
 */
package com.unikart.service;

import com.unikart.product.Product;
import com.unikart.product.ProductDAO;
import com.unikart.user.User;
import com.unikart.user.UserDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();

    // Verify the user's role using the database.
    private User requireRole(User authenticatedUser, User.Role requiredRole) {

        if (authenticatedUser == null || authenticatedUser.getUserId() <= 0) {
            throw new SecurityException("Authentication required!");
        }

        User databaseUser;

        try {
            databaseUser = userDAO.findById(authenticatedUser.getUserId())
                    .orElseThrow(() ->
                            new SecurityException("User not found!"));

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unable to verify user permissions!", e
            );
        }

        if (databaseUser.getRole() != requiredRole) {
            throw new SecurityException(
                    "Access denied! Required role: " + requiredRole
            );
        }

        return databaseUser;
    }

    // ADMIN: View all products.
    public List<Product> getAllProducts(User admin) {

        requireRole(admin, User.Role.ADMIN);

        return productDAO.getAllProducts();
    }

    // Public: View one product.
    public Product getProductById(int productId) {

        return productDAO.getProductById(productId);
    }

    // SELLER: Add a product.
    public boolean addSellerProduct(User authenticatedSeller, Product product) {

        User seller = requireRole(
                authenticatedSeller,
                User.Role.SELLER
        );

        validateProduct(product);

        product.setSellerId(seller.getUserId());

        return productDAO.addProduct(product);
    }

    // ADMIN: Add a Unikart-owned product.
    public boolean addAdminProduct(User authenticatedAdmin, Product product) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        validateProduct(product);

        product.setSellerId(null);

        return productDAO.addProduct(product);
    }

    // SELLER: Update their own product.
    public boolean updateProduct(User authenticatedSeller, Product product) {

        User seller = requireRole(
                authenticatedSeller,
                User.Role.SELLER
        );

        validateProduct(product);

        Product existing = productDAO.getProductById(
                product.getProductId()
        );

        if (existing == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existing.getSellerId() == null
                || existing.getSellerId() != seller.getUserId()) {

            throw new SecurityException(
                    "You cannot update another seller's product!"
            );
        }

        return productDAO.updateProduct(product, seller.getUserId());
    }

    // ADMIN: Update a Unikart-owned product.
    public boolean updateAdminProduct(User authenticatedAdmin, Product product) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        validateProduct(product);

        Product existing = productDAO.getProductById(
                product.getProductId()
        );

        if (existing == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existing.getSellerId() != null) {
            throw new SecurityException(
                    "This product is not owned by Unikart!"
            );
        }

        return productDAO.updateUnikartProduct(product);
    }

    // SELLER: Delete their own product.
    public boolean deleteProduct(User authenticatedSeller, int productId) {

        User seller = requireRole(
                authenticatedSeller,
                User.Role.SELLER
        );

        Product existing = productDAO.getProductById(productId);

        if (existing == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existing.getSellerId() == null
                || existing.getSellerId() != seller.getUserId()) {

            throw new SecurityException(
                    "You cannot delete another seller's product!"
            );
        }

        return productDAO.deleteProduct(productId, seller.getUserId());
    }

    // ADMIN: Delete a Unikart-owned product.
    public boolean deleteAdminProduct(User authenticatedAdmin, int productId) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        Product existing = productDAO.getProductById(productId);

        if (existing == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existing.getSellerId() != null) {
            throw new SecurityException(
                    "This product is not owned by Unikart!"
            );
        }

        return productDAO.deleteUnikartProduct(productId);
    }

    // SELLER: View their own products.
    public List<Product> getProductsBySellerId(User authenticatedSeller) {

        User seller = requireRole(
                authenticatedSeller,
                User.Role.SELLER
        );

        return productDAO.getProductsBySellerId(seller.getUserId());
    }

    // ADMIN: View pending products.
    public List<Product> getPendingProducts(User authenticatedAdmin) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        return productDAO.getPendingProducts();
    }

    // Public: View approved products.
    public List<Product> getApprovedProducts() {

        return productDAO.getApprovedProducts();
    }

    // ADMIN: View rejected products.
    public List<Product> getRejectedProducts(User authenticatedAdmin) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        return productDAO.getRejectedProducts();
    }

    // ADMIN: Approve a pending product.
    public boolean approveProduct(User authenticatedAdmin, int productId) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        return productDAO.approveProduct(productId);
    }

    // ADMIN: Reject a pending product.
    public boolean rejectProduct(
            User authenticatedAdmin,
            int productId,
            String reason
    ) {

        requireRole(authenticatedAdmin, User.Role.ADMIN);

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Rejection reason is required!"
            );
        }

        return productDAO.rejectProduct(productId, reason.trim());
    }

    // SELLER: Resubmit a rejected product.
    public boolean resubmitProduct(User authenticatedSeller, int productId) {

        User seller = requireRole(
                authenticatedSeller,
                User.Role.SELLER
        );

        Product product = productDAO.getProductById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (product.getSellerId() == null
                || product.getSellerId() != seller.getUserId()) {

            throw new SecurityException(
                    "You cannot resubmit another seller's product!"
            );
        }

        return productDAO.resubmitProduct(productId, seller.getUserId());
    }

    // Public: Search approved products.
    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty!"
            );
        }

        return productDAO.searchProducts(keyword.trim());
    }

    // Public: Filter products by category.
    public List<Product> getProductsByCategory(int categoryId) {

        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID!");
        }

        return productDAO.getProductsByCategory(categoryId);
    }

    // Public: Sort products by price.
    public List<Product> sortProductsByPrice(String order) {

        if (order == null
                || (!order.equalsIgnoreCase("ASC")
                && !order.equalsIgnoreCase("DESC"))) {

            throw new IllegalArgumentException(
                    "Sort order must be ASC or DESC!"
            );
        }

        return productDAO.sortProductsByPrice(order);
    }

    // Validate product details.
    private void validateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null!"
            );
        }

        if (product.getProductName() == null
                || product.getProductName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product name is required!"
            );
        }

        if (product.getProductName().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Product name cannot exceed 150 characters!"
            );
        }

        if (product.getCategoryId() <= 0) {
            throw new IllegalArgumentException(
                    "Valid category is required!"
            );
        }

        if (product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) <= 0
                || product.getPrice().compareTo(
                        new BigDecimal("99999999.99")) > 0) {

            throw new IllegalArgumentException(
                    "Product price must be greater than zero!"
            );
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException(
                    "Product stock cannot be negative!"
            );
        }
    }
}
