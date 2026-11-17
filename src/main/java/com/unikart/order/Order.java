
package com.unikart.order;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {

    private int orderId;
    private int customerId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private Timestamp createdAt;

    public Order(int orderId, int customerId,
                 BigDecimal totalAmount,
                 String orderStatus,
                 Timestamp createdAt) {

        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
