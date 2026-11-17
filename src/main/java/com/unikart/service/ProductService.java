
package com.unikart.service;

import com.unikart.product.Product;
import com.unikart.product.ProductDAO;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    // Get all products (admin use)
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    // Get product by ID
    public Product getProductById(int productId) {

        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID!");
        }

        return productDAO.getProductById(productId);
    }

    // Add a new product
    public boolean addProduct(Product product) {

        validateProduct(product);

        if (product.getSellerId() == null ||
                product.getSellerId() <= 0) {
            throw new IllegalArgumentException("Invalid seller ID!");
        }

        return productDAO.addProduct(product);
    }

    // Update an existing product
    public boolean updateProduct(Product product, int sellerId) {

        validateProduct(product);

        if (product.getProductId() <= 0 || sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product or seller ID!");
        }

        Product existingProduct =
                productDAO.getProductById(product.getProductId());

        if (existingProduct == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existingProduct.getSellerId() == null ||
                existingProduct.getSellerId() != sellerId) {
            throw new IllegalArgumentException(
                    "You cannot update another seller's product!");
        }

        return productDAO.updateProduct(product, sellerId);
    }

    // Delete a product
    public boolean deleteProduct(int productId, int sellerId) {

        if (productId <= 0 || sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product or seller ID!");
        }

        Product existingProduct = productDAO.getProductById(productId);

        if (existingProduct == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        if (existingProduct.getSellerId() == null ||
                existingProduct.getSellerId() != sellerId) {
            throw new IllegalArgumentException(
                    "You cannot delete another seller's product!");
        }

        return productDAO.deleteProduct(productId, sellerId);
    }

    // Get products belonging to a seller
    public List<Product> getProductsBySellerId(int sellerId) {

        if (sellerId <= 0) {
            throw new IllegalArgumentException("Invalid seller ID!");
        }

        return productDAO.getProductsBySellerId(sellerId);
    }

    // Get pending products
    public List<Product> getPendingProducts() {
        return productDAO.getPendingProducts();
    }

    // Get approved products
    public List<Product> getApprovedProducts() {
        return productDAO.getApprovedProducts();
    }

    // Get rejected products
    public List<Product> getRejectedProducts() {
        return productDAO.getRejectedProducts();
    }

    // Approve a product
    public boolean approveProduct(int productId) {

        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID!");
        }

        return productDAO.approveProduct(productId);
    }

    // Reject a product
    public boolean rejectProduct(int productId, String reason) {

        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID!");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Rejection reason cannot be empty!");
        }

        return productDAO.rejectProduct(productId, reason.trim());
    }

    // Resubmit a rejected product
    public boolean resubmitProduct(int productId, int sellerId) {

        if (productId <= 0 || sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product or seller ID!");
        }

        return productDAO.resubmitProduct(productId, sellerId);
    }

    // Search products
    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty!");
        }

        return productDAO.searchProducts(keyword.trim());
    }

    // Filter products by category
    public List<Product> getProductsByCategory(int categoryId) {

        if (categoryId <= 0) {
            throw new IllegalArgumentException("Invalid category ID!");
        }

        return productDAO.getProductsByCategory(categoryId);
    }

    // Sort products by price
    public List<Product> sortProductsByPrice(String order) {

        if (order == null ||
                (!order.equalsIgnoreCase("ASC") &&
                        !order.equalsIgnoreCase("DESC"))) {
            throw new IllegalArgumentException(
                    "Order must be ASC or DESC!");
        }

        return productDAO.sortProductsByPrice(order);
    }

    // Validate product details
    private void validateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null!");
        }

        if (product.getProductName() == null ||
                product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty!");
        }

        if (product.getProductName().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Product name cannot exceed 150 characters!");
        }

        if (product.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Invalid category ID!");
        }

        if (product.getPrice() == null ||
                product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero!");
        }

        if (product.getPrice().compareTo(
                new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("Product price is too high!");
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException(
                    "Product stock cannot be negative!");
        }
    }
}
