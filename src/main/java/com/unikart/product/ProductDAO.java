
package com.unikart.product;

import com.unikart.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private Product mapProduct(ResultSet result) throws SQLException {

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

    private List<Product> executeProductQuery(
            String sql,
            Integer parameter
    ) {

        List<Product> products = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setInt(1, parameter);
            }

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProduct(result));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products!", e);
        }

        return products;
    }

    // Fetch all products. Used by admin.
    public List<Product> getAllProducts() {

        String sql = "SELECT * FROM products ORDER BY product_id DESC";

        return executeProductQuery(sql, null);
    }

    // Add a product. seller_id can be NULL for Unikart-owned products.
    public boolean addProduct(Product product) {

        String sql = "INSERT INTO products "
                + "(seller_id, category_id, product_name, description, "
                + "price, stock, status) "
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

        } catch (SQLException e) {
            throw new RuntimeException("Error adding product!", e);
        }
    }

    // Find a product by ID.
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

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product!", e);
        }

        return null;
    }

    // Seller updates only their own product.
    public boolean updateProduct(Product product, int sellerId) {

        Product existing = getProductById(product.getProductId());

        if (existing == null || existing.getSellerId() == null
                || existing.getSellerId() != sellerId) {
            return false;
        }

        boolean materialChange =
                existing.getCategoryId() != product.getCategoryId()
                || !existing.getProductName().equals(product.getProductName())
                || !java.util.Objects.equals(
                        existing.getDescription(), product.getDescription())
                || existing.getPrice().compareTo(product.getPrice()) != 0;

        String sql = "UPDATE products SET category_id = ?, "
                + "product_name = ?, description = ?, price = ?, stock = ?, "
                + "status = ?, rejection_reason = ? "
                + "WHERE product_id = ? AND seller_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStock());

            statement.setString(
                    6,
                    materialChange ? "PENDING" : existing.getStatus()
            );

            statement.setString(
                    7,
                    materialChange ? null : existing.getRejectionReason()
            );

            statement.setInt(8, product.getProductId());
            statement.setInt(9, sellerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating seller product!", e);
        }
    }

    // Admin updates only products owned by Unikart.
    public boolean updateUnikartProduct(Product product) {

        Product existing = getProductById(product.getProductId());

        if (existing == null || existing.getSellerId() != null) {
            return false;
        }

        boolean materialChange =
                existing.getCategoryId() != product.getCategoryId()
                || !existing.getProductName().equals(product.getProductName())
                || !java.util.Objects.equals(
                        existing.getDescription(), product.getDescription())
                || existing.getPrice().compareTo(product.getPrice()) != 0;

        String sql = "UPDATE products SET category_id = ?, "
                + "product_name = ?, description = ?, price = ?, stock = ?, "
                + "status = ?, rejection_reason = ? "
                + "WHERE product_id = ? AND seller_id IS NULL";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStock());

            statement.setString(
                    6,
                    materialChange ? "PENDING" : existing.getStatus()
            );

            statement.setString(
                    7,
                    materialChange ? null : existing.getRejectionReason()
            );

            statement.setInt(8, product.getProductId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating Unikart product!", e);
        }
    }

    // Seller deletes only their own product.
    public boolean deleteProduct(int productId, int sellerId) {

        String sql = "DELETE FROM products "
                + "WHERE product_id = ? AND seller_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting seller product!", e);
        }
    }

    // Admin deletes only Unikart-owned products.
    public boolean deleteUnikartProduct(int productId) {

        String sql = "DELETE FROM products "
                + "WHERE product_id = ? AND seller_id IS NULL";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Unikart product!", e);
        }
    }

    // Approve only a pending product.
    public boolean approveProduct(int productId) {

        String sql = "UPDATE products SET status = 'APPROVED', "
                + "rejection_reason = NULL "
                + "WHERE product_id = ? AND status = 'PENDING'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error approving product!", e);
        }
    }

    // Reject only a pending product.
    public boolean rejectProduct(int productId, String reason) {

        String sql = "UPDATE products SET status = 'REJECTED', "
                + "rejection_reason = ? "
                + "WHERE product_id = ? AND status = 'PENDING'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, reason);
            statement.setInt(2, productId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error rejecting product!", e);
        }
    }

    public List<Product> getPendingProducts() {

        String sql = "SELECT * FROM products "
                + "WHERE status = 'PENDING' ORDER BY product_id DESC";

        return executeProductQuery(sql, null);
    }

    public List<Product> getApprovedProducts() {

        String sql = "SELECT * FROM products "
                + "WHERE status = 'APPROVED' ORDER BY product_id DESC";

        return executeProductQuery(sql, null);
    }

    public List<Product> getRejectedProducts() {

        String sql = "SELECT * FROM products "
                + "WHERE status = 'REJECTED' ORDER BY product_id DESC";

        return executeProductQuery(sql, null);
    }

    public List<Product> getProductsBySellerId(int sellerId) {

        String sql = "SELECT * FROM products "
                + "WHERE seller_id = ? ORDER BY product_id DESC";

        return executeProductQuery(sql, sellerId);
    }

    // Customer search: approved products only.
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

        } catch (SQLException e) {
            throw new RuntimeException("Error searching products!", e);
        }

        return products;
    }

    // Customer category filter: approved products only.
    public List<Product> getProductsByCategory(int categoryId) {

        String sql = "SELECT * FROM products "
                + "WHERE category_id = ? AND status = 'APPROVED' "
                + "ORDER BY product_id DESC";

        return executeProductQuery(sql, categoryId);
    }

    // Customer price sorting: approved products only.
    public List<Product> sortProductsByPrice(String order) {

        String sortOrder;

        if ("ASC".equalsIgnoreCase(order)) {
            sortOrder = "ASC";
        } else if ("DESC".equalsIgnoreCase(order)) {
            sortOrder = "DESC";
        } else {
            throw new IllegalArgumentException(
                    "Order must be ASC or DESC"
            );
        }

        String sql = "SELECT * FROM products "
                + "WHERE status = 'APPROVED' "
                + "ORDER BY price " + sortOrder;

        return executeProductQuery(sql, null);
    }

    // Seller resubmits only their own rejected product.
    public boolean resubmitProduct(int productId, int sellerId) {

        String sql = "UPDATE products SET status = 'PENDING', "
                + "rejection_reason = NULL "
                + "WHERE product_id = ? AND seller_id = ? "
                + "AND status = 'REJECTED'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error resubmitting product!", e);
        }
    }
}
