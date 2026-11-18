package com.unikart.order;

public class OrderStatusTest {

    public static void main(String[] args) {

        OrderService orderService = new OrderService();

        System.out.println("===== ORDER STATUS TEST =====");

        boolean updated =
                orderService.updateOrderStatus(62, "PROCESSING");

        if (updated) {
            System.out.println("Order status updated successfully!");
        } else {
            System.out.println("Order status update failed!");
        }

        Order order = orderService.getOrder(62);

        if (order != null) {
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Current Status: " + order.getOrderStatus());
        }

        System.out.println("==============================");
    }
}
