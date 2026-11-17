
package com.unikart.product;

import com.unikart.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Fetch all products (for admin use)
    public List<Product> getAllProducts() {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(mapProduct(result));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching products!", e);
        }

        return products;
    }

    // Add a new product
    public boolean addProduct(Product product) {

        String sql = "INSERT INTO products "
                + "(seller_id, category_id, product_name, description, price, stock, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            if (product.getSellerId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, product.getSellerId());
            }

            statement.setInt(2, product.getCategoryId());
            statement.setString(3, product.getProductName());
            statement.setString(4, product.getDescription());
            statement.setBigDecimal(5, product.getPrice());
            statement.setInt(6, product.getStock());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error adding product!", e);
        }
    }

    // Fetch one product using its ID
    public Product getProductById(int productId) {

        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapProduct(result);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching product!", e);
        }

        return null;
    }

    // Update an existing product.
    // Material changes send the product back for admin approval.
    // Stock-only changes do not affect approval status.
    public boolean updateProduct(Product product, int sellerId) {

        String sql = "UPDATE products SET "
                + "status = CASE "
                + "WHEN NOT (category_id <=> ?) "
                + "OR NOT (product_name <=> ?) "
                + "OR NOT (description <=> ?) "
                + "OR NOT (price <=> ?) "
                + "THEN 'PENDING' ELSE status END, "
                + "rejection_reason = CASE "
                + "WHEN NOT (category_id <=> ?) "
                + "OR NOT (product_name <=> ?) "
                + "OR NOT (description <=> ?) "
                + "OR NOT (price <=> ?) "
                + "THEN NULL ELSE rejection_reason END, "
                + "category_id = ?, "
                + "product_name = ?, "
                + "description = ?, "
                + "price = ?, "
                + "stock = ? "
                + "WHERE product_id = ? AND seller_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());

            statement.setInt(5, product.getCategoryId());
            statement.setString(6, product.getProductName());
            statement.setString(7, product.getDescription());
            statement.setBigDecimal(8, product.getPrice());

            statement.setInt(9, product.getCategoryId());
            statement.setString(10, product.getProductName());
            statement.setString(11, product.getDescription());
            statement.setBigDecimal(12, product.getPrice());
            statement.setInt(13, product.getStock());

            statement.setInt(14, product.getProductId());
            statement.setInt(15, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error updating product!", e);
        }
    }

    // Delete a product belonging to a particular seller
    public boolean deleteProduct(int productId, int sellerId) {

        String sql = "DELETE FROM products "
                + "WHERE product_id = ? AND seller_id = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error deleting product!", e);
        }
    }

    // Approve a product only if it is pending
    public boolean approveProduct(int productId) {

        String sql = "UPDATE products SET status = 'APPROVED', "
                + "rejection_reason = NULL "
                + "WHERE product_id = ? AND status = 'PENDING'";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error approving product!", e);
        }
    }

    // Reject a product only if it is pending
    public boolean rejectProduct(int productId, String reason) {

        String sql = "UPDATE products SET status = 'REJECTED', "
                + "rejection_reason = ? "
                + "WHERE product_id = ? AND status = 'PENDING'";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reason);
            statement.setInt(2, productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error rejecting product!", e);
        }
    }

    // Fetch products waiting for admin approval
    public List<Product> getPendingProducts() {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE status = 'PENDING' "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(mapProduct(result));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching pending products!", e);
        }

        return products;
    }

    // Fetch approved products
    public List<Product> getApprovedProducts() {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE status = 'APPROVED' "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(mapProduct(result));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching approved products!", e);
        }

        return products;
    }

    // Fetch rejected products
    public List<Product> getRejectedProducts() {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE status = 'REJECTED' "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(mapProduct(result));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching rejected products!", e);
        }

        return products;
    }

    // Fetch products belonging to a seller
    public List<Product> getProductsBySellerId(int sellerId) {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE seller_id = ? "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProduct(result));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching seller products!", e);
        }

        return products;
    }

    // Search approved products by name
    public List<Product> searchProducts(String keyword) {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE product_name LIKE ? "
                + "AND status = 'APPROVED' "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + keyword + "%");

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProduct(result));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error searching products!", e);
        }

        return products;
    }

    // Filter approved products by category
    public List<Product> getProductsByCategory(int categoryId) {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products "
                + "WHERE category_id = ? "
                + "AND status = 'APPROVED' "
                + "ORDER BY product_id DESC";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProduct(result));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error filtering products!", e);
        }

        return products;
    }

    // Sort approved products by price
    public List<Product> sortProductsByPrice(String order) {

        List<Product> products = new ArrayList<>();

        String sortOrder;

        if ("ASC".equalsIgnoreCase(order)) {
            sortOrder = "ASC";
        } else if ("DESC".equalsIgnoreCase(order)) {
            sortOrder = "DESC";
        } else {
            throw new IllegalArgumentException(
                    "Order must be ASC or DESC");
        }

        String sql = "SELECT * FROM products "
                + "WHERE status = 'APPROVED' "
                + "ORDER BY price " + sortOrder;

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(mapProduct(result));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error sorting products!", e);
        }

        return products;
    }

    // Resubmit a rejected product belonging to a particular seller
    public boolean resubmitProduct(int productId, int sellerId) {

        String sql = "UPDATE products SET "
                + "status = 'PENDING', "
                + "rejection_reason = NULL "
                + "WHERE product_id = ? "
                + "AND seller_id = ? "
                + "AND status = 'REJECTED'";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException("Error resubmitting product!", e);
        }
    }

    // Convert a database row into a Product object
    private Product mapProduct(ResultSet result) throws Exception {

        Product product = new Product();

        product.setProductId(result.getInt("product_id"));

        int sellerId = result.getInt("seller_id");
        product.setSellerId(result.wasNull() ? null : sellerId);

        product.setCategoryId(result.getInt("category_id"));
        product.setProductName(result.getString("product_name"));
        product.setDescription(result.getString("description"));
        product.setPrice(result.getBigDecimal("price"));
        product.setStock(result.getInt("stock"));
        product.setStatus(result.getString("status"));
        product.setRejectionReason(result.getString("rejection_reason"));
        product.setCreatedAt(result.getTimestamp("created_at"));

        return product;
    }
}
