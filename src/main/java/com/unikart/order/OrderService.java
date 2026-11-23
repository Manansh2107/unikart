/*
 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();

    private final CheckoutService checkoutService =
            new CheckoutService();

    // Place an order using the customer's cart.
    public int placeOrder(int customerId, String paymentMethod) {

        return checkoutService.checkout(customerId, paymentMethod);
    }

    // Get one order.
    public Order getOrder(int orderId) {

        if (orderId <= 0) {
            System.out.println("Invalid order ID!");
            return null;
        }

        return orderDAO.getOrderById(orderId);
    }

    // Get all orders of a customer.
    public List<Order> getCustomerOrders(int customerId) {

        if (customerId <= 0) {
            System.out.println("Invalid customer ID!");
            return List.of();
        }

        return orderDAO.getOrdersByCustomerId(customerId);
    }

    // Update an order's status.
    public boolean updateOrderStatus(int orderId, String status) {

        if (orderId <= 0 || status == null) {
            return false;
        }

        List<String> validStatuses = List.of(
                "PLACED",
                "PROCESSING",
                "SHIPPED",
                "DELIVERED",
                "CANCELLED"
        );

        if (!validStatuses.contains(status)) {
            System.out.println("Invalid order status!");
            return false;
        }

        return orderDAO.updateOrderStatus(orderId, status);
    }
}
