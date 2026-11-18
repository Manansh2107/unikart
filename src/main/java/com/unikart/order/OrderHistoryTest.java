package com.unikart.order;

import java.util.List;

public class OrderHistoryTest {

    public static void main(String[] args) {

        OrderService orderService = new OrderService();

        int customerId = 2;

        List<Order> orders =
                orderService.getCustomerOrders(customerId);

        System.out.println("===== CUSTOMER ORDER HISTORY =====");
        System.out.println("Customer ID: " + customerId);

        if (orders.isEmpty()) {
            System.out.println("No orders found!");
        } else {
            for (Order order : orders) {
                System.out.println("------------------------------");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Total Amount: " + order.getTotalAmount());
                System.out.println("Status: " + order.getOrderStatus());
                System.out.println("Date: " + order.getCreatedAt());
            }
        }

        System.out.println("=================================");
    }
}
