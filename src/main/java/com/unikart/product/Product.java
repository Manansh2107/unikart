
package com.unikart.product;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {

    private int productId;
    private Integer sellerId;
    private int categoryId;
    private String productName;
    private String description;
    private BigDecimal price;
    private int stock;
    private String status;
    private String rejectionReason;
    private Timestamp createdAt;

    // Default constructor
    public Product() {
    }

    // Parameterized constructor
    public Product(int productId, Integer sellerId, int categoryId,
            String productName, String description,
            BigDecimal price, int stock, String status,
            String rejectionReason, Timestamp createdAt) {

        this.productId = productId;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}


