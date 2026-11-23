/*

 * Developers: Himanshu, MANANSH GOYAL
 * Development Dates: 19-11-2026 and 22-11-2026
 */
package com.unikart.customer;

import com.unikart.cart.Cart;
import com.unikart.cart.CartItem;
import com.unikart.cart.CartService;
import com.unikart.order.Order;
import com.unikart.order.OrderService;
import com.unikart.order.Payment;
import com.unikart.order.PaymentDAO;
import com.unikart.product.Category;
import com.unikart.product.CategoryDAO;
import com.unikart.product.Product;
import com.unikart.service.ProductService;
import com.unikart.user.SellerService;
import com.unikart.user.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {

    private final User customer;
    private final Scanner scanner;

    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final SellerService sellerService = new SellerService();

    public CustomerMenu(User customer, Scanner scanner) {
        this.customer = customer;
        this.scanner = scanner;
    }

    public void showMenu() {

        if (customer == null
                || customer.getRole() != User.Role.CUSTOMER) {
            System.out.println("Access denied!");
            return;
        }

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("       WELCOME TO UNIKART");
            System.out.println("       CUSTOMER PANEL");
            System.out.println("================================");
            System.out.println("1. Browse Products");
            System.out.println("2. Search Products");
            System.out.println("3. Browse by Category");
            System.out.println("4. Sort Products by Price");
            System.out.println("5. Add Product to Cart");
            System.out.println("6. View Cart");
            System.out.println("7. Update Cart Quantity");
            System.out.println("8. Remove Cart Item");
            System.out.println("9. Checkout");
            System.out.println("10. Order History");
            System.out.println("11. View Payment Details");
            System.out.println("12. Apply for Seller Account");
            System.out.println("13. Logout");
            System.out.println("================================");

            int choice = readInt("Enter your choice: ");

            try {
                switch (choice) {

                    case 1:
                        browseProducts();
                        break;

                    case 2:
                        searchProducts();
                        break;

                    case 3:
                        browseByCategory();
                        break;

                    case 4:
                        sortProducts();
                        break;

                    case 5:
                        addToCart();
                        break;

                    case 6:
                        viewCart();
                        break;

                    case 7:
                        updateCartQuantity();
                        break;

                    case 8:
                        removeCartItem();
                        break;

                    case 9:
                        checkout();
                        break;

                    case 10:
                        viewOrderHistory();
                        break;

                    case 11:
                        viewPaymentDetails();
                        break;

                    case 12:
                        applyForSellerAccount();
                        break;

                    case 13:
                        running = false;
                        System.out.println("Logged out successfully!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void browseProducts() {

        List<Product> products = productService.getApprovedProducts();

        displayProducts(products);
    }

    private void searchProducts() {

        System.out.print("Enter product name: ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be empty!");
            return;
        }

        displayProducts(productService.searchProducts(keyword));
    }

    private void browseByCategory() {

        List<Category> categories = categoryDAO.getAllCategories();

        System.out.println("\n========== CATEGORIES ==========");

        for (Category category : categories) {
            System.out.println(
                    category.getCategoryId() + ". "
                            + category.getCategoryName());
        }

        int categoryId = readInt("Enter category ID: ");

        displayProducts(
                productService.getProductsByCategory(categoryId));
    }

    private void sortProducts() {

        System.out.println("\n1. Price: Low to High");
        System.out.println("2. Price: High to Low");

        int choice = readInt("Enter choice: ");

        if (choice == 1) {
            displayProducts(
                    productService.sortProductsByPrice("ASC"));
        } else if (choice == 2) {
            displayProducts(
                    productService.sortProductsByPrice("DESC"));
        } else {
            System.out.println("Invalid choice!");
        }
    }

    private void displayProducts(List<Product> products) {

        if (products == null || products.isEmpty()) {
            System.out.println("No products found!");
            return;
        }

        System.out.println("\n========== PRODUCTS ==========");

        for (Product product : products) {

            System.out.println("--------------------------------");
            System.out.println("Product ID: " + product.getProductId());
            System.out.println("Name: " + product.getProductName());
            System.out.println("Description: " + product.getDescription());
            System.out.println("Price: Rs. " + product.getPrice());
            System.out.println("Available Stock: " + product.getStock());
        }

        System.out.println("--------------------------------");
    }

    private void addToCart() {

        browseProducts();

        int productId = readInt("Enter product ID to add: ");
        int quantity = readInt("Enter quantity: ");

        Product product = productService.getProductById(productId);

        if (product == null
                || !"APPROVED".equalsIgnoreCase(product.getStatus())) {
            System.out.println("Product not available!");
            return;
        }

        Cart cart = cartService.getOrCreateCart(customer);

        if (cart == null) {
            System.out.println("Unable to access cart!");
            return;
        }

        if (cartService.addItem(cart.getCartId(), productId, quantity)) {
            System.out.println("Product added to cart successfully!");
        } else {
            System.out.println("Unable to add product to cart!");
        }
    }

    private void viewCart() {

        Cart cart = cartService.getOrCreateCart(customer);

        if (cart == null) {
            System.out.println("Unable to access cart!");
            return;
        }

        List<CartItem> items = cartService.getCartItems(cart.getCartId());

        if (items.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }

        System.out.println("\n========== YOUR CART ==========");

        for (CartItem item : items) {

            System.out.println("--------------------------------");
            System.out.println("Product ID: " + item.getProductId());
            System.out.println("Product: " + item.getProductName());
            System.out.println("Price: Rs. " + item.getUnitPrice());
            System.out.println("Quantity: " + item.getQuantity());
            System.out.printf(
                    "Subtotal: Rs. %.2f%n",
                    item.getUnitPrice() * item.getQuantity());
        }

        System.out.println("--------------------------------");
        System.out.printf(
                "Cart Total: Rs. %.2f%n",
                cartService.getCartTotal(cart.getCartId()));
    }

    private void updateCartQuantity() {

        Cart cart = cartService.getOrCreateCart(customer);

        if (cart == null) {
            System.out.println("Unable to access cart!");
            return;
        }

        viewCart();

        int productId = readInt("Enter product ID: ");
        int quantity = readInt("Enter new quantity: ");

        if (cartService.updateQuantity(
                cart.getCartId(), productId, quantity)) {

            System.out.println("Cart updated successfully!");
        } else {
            System.out.println("Unable to update cart!");
        }
    }

    private void removeCartItem() {

        Cart cart = cartService.getOrCreateCart(customer);

        if (cart == null) {
            System.out.println("Unable to access cart!");
            return;
        }

        viewCart();

        int productId = readInt("Enter product ID to remove: ");

        if (cartService.removeItem(cart.getCartId(), productId)) {
            System.out.println("Product removed from cart!");
        } else {
            System.out.println("Unable to remove product!");
        }
    }

    private void checkout() {

        Cart cart = cartService.getOrCreateCart(customer);

        if (cart == null
                || cartService.getCartItems(cart.getCartId()).isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }

        viewCart();

        System.out.println("\n========== PAYMENT METHOD ==========");
        System.out.println("1. UPI");
        System.out.println("2. Card");
        System.out.println("3. Cash on Delivery");

        int choice = readInt("Select payment method: ");

        String paymentMethod;

        switch (choice) {
            case 1:
                paymentMethod = "UPI";
                break;

            case 2:
                paymentMethod = "CARD";
                break;

            case 3:
                paymentMethod = "COD";
                break;

            default:
                System.out.println("Invalid payment method!");
                return;
        }

        System.out.print("Confirm checkout? (Y/N): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Checkout cancelled.");
            return;
        }

        int orderId = orderService.placeOrder(
                customer.getUserId(),
                paymentMethod);

        if (orderId > 0) {
            System.out.println("Order placed successfully!");
            System.out.println("Your Order ID: " + orderId);
        } else {
            System.out.println("Checkout failed!");
        }
    }

    private void viewOrderHistory() {

        List<Order> orders = orderService.getCustomerOrders(
                customer.getUserId());

        if (orders == null || orders.isEmpty()) {
            System.out.println("No orders found!");
            return;
        }

        System.out.println("\n========== ORDER HISTORY ==========");

        for (Order order : orders) {

            System.out.println("--------------------------------");
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Total: Rs. " + order.getTotalAmount());
            System.out.println("Status: " + order.getOrderStatus());
            System.out.println("Date: " + order.getCreatedAt());
        }

        System.out.println("--------------------------------");
    }

    private void viewPaymentDetails() {

        int orderId = readInt("Enter order ID: ");

        Order order = orderService.getOrder(orderId);

        if (order == null
                || order.getCustomerId() != customer.getUserId()) {
            System.out.println("Order not found!");
            return;
        }

        Payment payment = paymentDAO.getPaymentByOrderId(orderId);

        if (payment == null) {
            System.out.println("Payment details not found!");
            return;
        }

        System.out.println("\n========== PAYMENT DETAILS ==========");
        System.out.println("Payment ID: " + payment.getPaymentId());
        System.out.println("Order ID: " + payment.getOrderId());
        System.out.println("Amount: Rs. " + payment.getAmount());
        System.out.println("Method: " + payment.getPaymentMethod());
        System.out.println("Status: " + payment.getPaymentStatus());
        System.out.println(
                "Transaction Reference: "
                        + payment.getTransactionReference());
        System.out.println("Date: " + payment.getCreatedAt());
    }

    private void applyForSellerAccount() throws SQLException {

        System.out.println("\n========== SELLER APPLICATION ==========");

        System.out.println(
                "Apply to become a seller on Unikart.");

        System.out.print("Enter your college name: ");
        String collegeName = scanner.nextLine().trim();

        if (collegeName.isEmpty()) {
            System.out.println("College name is required!");
            return;
        }

        System.out.print("Why do you want to become a seller? ");
        String reason = scanner.nextLine().trim();

        int applicationId = sellerService.applyForSeller(
                customer,
                collegeName,
                reason
        );

        System.out.println("\nSeller application submitted successfully!");
        System.out.println("Application ID: " + applicationId);
        System.out.println("Status: PENDING");
        System.out.println("An admin will review your application.");
    }

    private int readInt(String message) {

        while (true) {

            System.out.print(message);
            String value = scanner.nextLine().trim();

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
}
