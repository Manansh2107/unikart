package com.unikart.cart;

import java.util.List;
import java.util.ArrayList;

public class Cart {
    private int cartId;
    private int userId;
    private List<CartItem> items;

    public Cart(int cartId, int userId) {

    this.cartId = cartId;
    this.userId = userId;
    this.items = new ArrayList<>();

}
public void addItem(CartItem item) {
    items.add(item);
}

public void removeItem(int productId) {

    items.removeIf(item -> item.getProductId() == productId);

}

public List<CartItem> getItems() {
    return items;
}

public int getCartId() {
    return cartId;
}

public int getUserId() {
    return userId;
}
}
