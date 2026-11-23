/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.order;

public class InvalidOrderStatusTest {

    public static void main(String[] args) {

        OrderService orderService = new OrderService();

        System.out.println("===== INVALID STATUS TEST =====");

        boolean updated =
                orderService.updateOrderStatus(62, "DELIVERED_NOW");

        if (updated) {
            System.out.println("Test failed! Invalid status was accepted.");
        } else {
            System.out.println("Test passed! Invalid status was rejected.");
        }

        Order order = orderService.getOrder(62);

        System.out.println("Current Status: " + order.getOrderStatus());

        System.out.println("================================");
    }
}
s