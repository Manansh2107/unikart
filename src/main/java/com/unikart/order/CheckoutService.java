/*

 * Developer: MANANSH GOYAL
 * Development Date: 17-11-2026
 */
package com.unikart.order;

import com.unikart.database.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CheckoutService {

    private final OrderItemDAO orderItemDAO = new OrderItemDAO();

    private final SellerOrderGroupDAO sellerOrderGroupDAO =
            new SellerOrderGroupDAO();

    private final PaymentDAO paymentDAO = new PaymentDAO();

    // Stores the details of each product being purchased.
    private static class CheckoutLine {

        int productId;
        String productName;
        int quantity;
        BigDecimal price;
        Integer sellerId;

        CheckoutLine(int productId, String productName,
                     int quantity, BigDecimal price,
                     Integer sellerId) {

            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.sellerId = sellerId;
        }
    }

    // Main checkout method.
    public int checkout(int customerId, String paymentMethod) {

        if (customerId <= 0 ||
                paymentMethod == null ||
                paymentMethod.isBlank()) {

            System.out.println("Invalid checkout details!");
            return -1;
        }

        String cartSql =
                "SELECT cart_id FROM carts WHERE user_id = ? FOR UPDATE";

        String itemsSql = """
                SELECT ci.product_id,
                       ci.quantity,
                       p.product_name,
                       p.price,
                       p.stock,
                       p.status,
                       p.seller_id
                FROM cart_items ci
                JOIN products p ON ci.product_id = p.product_id
                WHERE ci.cart_id = ?
                ORDER BY ci.product_id
                FOR UPDATE
                """;

        String orderSql = """
                INSERT INTO orders (customer_id, total_amount)
                VALUES (?, ?)
                """;

        String stockSql = """
                UPDATE products
                SET stock = stock - ?
                WHERE product_id = ?
                  AND stock >= ?
                  AND status = 'APPROVED'
                """;

        String clearCartSql =
                "DELETE FROM cart_items WHERE cart_id = ?";

        try (Connection connection = DBConnection.getConnection()) {

            // Start database transaction.
            connection.setAutoCommit(false);

            try {

                // STEP 1: Find and lock customer's cart.
                int cartId;

                try (PreparedStatement ps =
                             connection.prepareStatement(cartSql)) {

                    ps.setInt(1, customerId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Customer cart not found!"
                            );
                        }

                        cartId = rs.getInt("cart_id");
                    }
                }

                // STEP 2: Retrieve and lock cart products.
                List<CheckoutLine> lines = new ArrayList<>();

                BigDecimal totalAmount = BigDecimal.ZERO;

                try (PreparedStatement ps =
                             connection.prepareStatement(itemsSql)) {

                    ps.setInt(1, cartId);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            int productId =
                                    rs.getInt("product_id");

                            int quantity =
                                    rs.getInt("quantity");

                            String productName =
                                    rs.getString("product_name");

                            BigDecimal price =
                                    rs.getBigDecimal("price");

                            int stock =
                                    rs.getInt("stock");

                            String status =
                                    rs.getString("status");

                            int sellerValue =
                                    rs.getInt("seller_id");

                            Integer sellerId = rs.wasNull()
                                    ? null : sellerValue;

                            // Validate product approval.
                            if (!"APPROVED".equals(status)) {
                                throw new SQLException(
                                        productName +
                                        " is not approved!"
                                );
                            }

                            // Validate quantity and stock.
                            if (quantity <= 0 || quantity > stock) {
                                throw new SQLException(
                                        "Insufficient stock for: " +
                                        productName
                                );
                            }

                            CheckoutLine line = new CheckoutLine(
                                    productId,
                                    productName,
                                    quantity,
                                    price,
                                    sellerId
                            );

                            lines.add(line);

                            BigDecimal subtotal = price.multiply(
                                    BigDecimal.valueOf(quantity)
                            );

                            totalAmount =
                                    totalAmount.add(subtotal);
                        }
                    }
                }

                // Prevent empty checkout.
                if (lines.isEmpty()) {
                    throw new SQLException(
                            "Your cart is empty!"
                    );
                }

                // STEP 3: Create main order.
                int orderId;

                try (PreparedStatement ps =
                             connection.prepareStatement(
                                     orderSql,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, customerId);
                    ps.setBigDecimal(2, totalAmount);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Order creation failed!"
                            );
                        }

                        orderId = rs.getInt(1);
                    }
                }

                // STEP 4: Create seller groups and order items.
                Map<Integer, Integer> sellerGroups =
                        new LinkedHashMap<>();

                for (CheckoutLine line : lines) {

                    Integer sellerId = line.sellerId;

                    Integer groupId = sellerGroups.get(sellerId);

                    // Create one group for each seller.
                    if (groupId == null) {

                        groupId =
                                sellerOrderGroupDAO.createSellerOrderGroup(
                                        connection,
                                        orderId,
                                        sellerId
                                );

                        sellerGroups.put(sellerId, groupId);
                    }

                    // Save product inside its seller group.
                    boolean itemSaved =
                            orderItemDAO.createOrderItem(
                                    connection,
                                    groupId,
                                    line.productId,
                                    line.productName,
                                    line.quantity,
                                    line.price
                            );

                    if (!itemSaved) {
                        throw new SQLException(
                                "Could not save order item!"
                        );
                    }
                }

                // STEP 5: Update inventory safely.
                try (PreparedStatement ps =
                             connection.prepareStatement(stockSql)) {

                    for (CheckoutLine line : lines) {

                        ps.setInt(1, line.quantity);
                        ps.setInt(2, line.productId);
                        ps.setInt(3, line.quantity);

                        int updated = ps.executeUpdate();

                        if (updated != 1) {
                            throw new SQLException(
                                    "Stock update failed for: " +
                                    line.productName
                            );
                        }
                    }
                }

                // STEP 6: Create simulated successful payment.
                int paymentId = paymentDAO.createPayment(
                        connection,
                        orderId,
                        totalAmount,
                        paymentMethod,
                        "SUCCESS",
                        "SIM-ORDER-" + orderId
                );

                if (paymentId <= 0) {
                    throw new SQLException(
                            "Payment record creation failed!"
                    );
                }

                // STEP 7: Clear customer's cart.
                try (PreparedStatement ps =
                             connection.prepareStatement(clearCartSql)) {

                    ps.setInt(1, cartId);
                    ps.executeUpdate();
                }

                // STEP 8: Commit all database changes.
                connection.commit();

                System.out.println(
                        "================================"
                );

                System.out.println("ORDER PLACED SUCCESSFULLY!");

                System.out.println("Order ID: " + orderId);

                System.out.println(
                        "Total Amount: " + totalAmount
                );

                System.out.println(
                        "Payment Status: SUCCESS (SIMULATED)"
                );

                System.out.println(
                        "Seller Groups: " + sellerGroups.size()
                );

                System.out.println(
                        "================================"
                );

                return orderId;

            } catch (Exception e) {

                // Undo all changes if any operation fails.
                connection.rollback();

                System.out.println(
                        "Checkout failed! Transaction rolled back."
                );

                System.out.println(e.getMessage());

                return -1;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Database error during checkout!"
            );

            e.printStackTrace();

            return -1;
        }
    }
}
