/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

public class CheckoutTest {

    public static void main(String[] args) {

        CheckoutService checkoutService = new CheckoutService();

        System.out.println("===== UNIKART CHECKOUT TEST =====");

        int orderId = checkoutService.checkout(2, "TEST");

        if (orderId > 0) {

            System.out.println("Checkout test passed!");
            System.out.println("Generated Order ID: " + orderId);

        } else {

            System.out.println("Checkout test failed!");
        }
    }
}
