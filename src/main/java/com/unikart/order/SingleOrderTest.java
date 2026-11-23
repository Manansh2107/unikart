/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.order;

public class SingleOrderTest {

    public static void main(String[] args) {

        OrderService orderService = new OrderService();

        Order order = orderService.getOrder(62);

        System.out.println("===== SINGLE ORDER DETAILS =====");

        if (order != null) {
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Customer ID: " + order.getCustomerId());
            System.out.println("Amount: " + order.getTotalAmount());
            System.out.println("Status: " + order.getOrderStatus());
            System.out.println("Date: " + order.getCreatedAt());
        } else {
            System.out.println("Order not found!");
        }

        System.out.println("================================");
    }
}
