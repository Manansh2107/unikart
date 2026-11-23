/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

import java.math.BigDecimal;

public class OrderItem {

    private int orderItemId;
    private int groupId;
    private int productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public OrderItem(int orderItemId, int groupId, int productId,
                     String productName, int quantity,
                     BigDecimal unitPrice, BigDecimal subtotal) {

        this.orderItemId = orderItemId;
        this.groupId = groupId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
