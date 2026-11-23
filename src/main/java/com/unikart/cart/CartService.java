/*

 * Developer: Himanshu
 * Development Date: 19-11-2026
 */
package com.unikart.cart;

import java.util.List;

import com.unikart.user.User;

public class CartService {

    private final CartDAO cartDAO = new CartDAO();

    // Get customer's cart or create one if it does not exist.
    public Cart getOrCreateCart(User customer) {

        if (!isValidCustomer(customer)) {
            System.out.println("Invalid customer!");
            return null;
        }

        Cart cart = cartDAO.getCartByUserId(customer.getUserId());

        if (cart == null) {
            cart = cartDAO.createCart(customer.getUserId());
        }

        return cart;
    }

    // Add a product to the cart.
    public boolean addItem(int cartId, int productId, int quantity) {

        if (cartId <= 0 || productId <= 0 || quantity <= 0) {
            System.out.println("Invalid cart details!");
            return false;
        }

        return cartDAO.addItemToCart(cartId, productId, quantity);
    }

    // Get all cart items.
    public List<CartItem> getCartItems(int cartId) {

        if (cartId <= 0) {
            return List.of();
        }

        return cartDAO.getCartItems(cartId);
    }

    // Update product quantity.
    public boolean updateQuantity(int cartId, int productId, int quantity) {

        if (cartId <= 0 || productId <= 0 || quantity <= 0) {
            System.out.println("Invalid quantity or product!");
            return false;
        }

        return cartDAO.updateCartItemQuantity(
                cartId,
                productId,
                quantity);
    }

    // Remove a product from the cart.
    public boolean removeItem(int cartId, int productId) {

        if (cartId <= 0 || productId <= 0) {
            return false;
        }

        return cartDAO.removeItemFromCart(cartId, productId);
    }

    // Get total cart amount.
    public double getCartTotal(int cartId) {

        if (cartId <= 0) {
            return 0.0;
        }

        return cartDAO.getCartTotal(cartId);
    }

    // Clear the cart.
    public boolean clearCart(int cartId) {

        if (cartId <= 0) {
            return false;
        }

        return cartDAO.clearCart(cartId);
    }

    // Validate customer role.
    private boolean isValidCustomer(User customer) {

        return customer != null
                && customer.getUserId() > 0
                && customer.getRole() == User.Role.CUSTOMER;
    }
}
