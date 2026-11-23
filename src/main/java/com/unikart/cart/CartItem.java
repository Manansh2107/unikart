/*

 * Developer: MANANSH GOYAL
 * Development Date: 16-11-2026
 */
package com.unikart.cart;

public class CartItem {

    private int cartItemId;
    private int cartId;
    private int productId;
    private String productName;
    private double unitPrice;
    private int quantity;



public CartItem(int cartItemId, int cartId, int productId,
                String productName, double unitPrice, int quantity) {

    this.cartItemId = cartItemId;
    this.cartId = cartId;
    this.productId = productId;
    this.productName = productName;
    this.unitPrice = unitPrice;
    this.quantity = quantity;
}
public int getCartItemId() {
    return cartItemId;
}

public int getCartId() {
    return cartId;
}

public int getProductId() {
    return productId;
}

public String getProductName() {
    return productName;
}

public double getUnitPrice() {
    return unitPrice;
}

public int getQuantity() {
    return quantity;
}

public void setQuantity(int quantity) {
    this.quantity = quantity;
}

}