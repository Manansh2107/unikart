
package com.unikart.cart;

import java.util.List;

public class CartTest {

    public static void main(String[] args) {

        CartDAO cartDAO = new CartDAO();

        // Test 1: Get customer's cart
        Cart cart = cartDAO.getCartByUserId(1);

        if (cart == null) {
            System.out.println("Cart not found!");
            return;
        }

        System.out.println("Cart found! Cart ID: " + cart.getCartId());

        // //  Test 2: Add products
        // System.out.println("Add Notebook: " +
        //         cartDAO.addItemToCart(1, 1, 2));

        // System.out.println("Add Pen: " +
        //         cartDAO.addItemToCart(1, 2, 3));

        // Test 3: Display cart items
        List<CartItem> items = cartDAO.getCartItems(1);

        System.out.println("\nCart Items:");

        for (CartItem item : items) {
            System.out.println(
                    item.getProductName() +
                    " | Quantity: " + item.getQuantity() +
                    " | Price: " + item.getUnitPrice()
            );
        }

        // Test 4: Display total
        System.out.println("\nCart Total: Rs." +
                cartDAO.getCartTotal(1));
        // Test 5: Update notebook quantity
        System.out.println("\nUpdate Notebook Quantity: " +
                cartDAO.updateCartItemQuantity(1, 1, 5));

        // Display updated cart total
        System.out.println("Updated Cart Total: Rs. " +
                cartDAO.getCartTotal(1));


         // Test 6: Remove notebook from cart
        System.out.println("\nRemove Notebook: " +
                cartDAO.removeItemFromCart(1, 1));

        // Display cart after removal
        List<CartItem> updatedItems = cartDAO.getCartItems(1);

        System.out.println("\nCart After Removing Notebook:");

        for (CartItem item : updatedItems) {
            System.out.println(
                    item.getProductName() +
                    " | Quantity: " + item.getQuantity() +
                    " | Price: " + item.getUnitPrice()
            );
        }

        System.out.println("New Cart Total: Rs. " +
                cartDAO.getCartTotal(1)); 
        // Test 7: Clear entire cart
        System.out.println("\nClear Cart: " +
                cartDAO.clearCart(1));

        // Display cart after clearing
        List<CartItem> finalItems = cartDAO.getCartItems(1);

        System.out.println("Items remaining: " + finalItems.size());

        System.out.println("Final Cart Total: Rs. " +
                cartDAO.getCartTotal(1));      
    }
}
