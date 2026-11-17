
package com.unikart.product;

import java.util.List;

public class ProductTest {

    public static void main(String[] args) {

        ProductDAO productDAO = new ProductDAO();

        // 1. Test Pending Products
        System.out.println("\n===== PENDING PRODUCTS =====");

        List<Product> pendingProducts = productDAO.getPendingProducts();

        if (pendingProducts.isEmpty()) {
            System.out.println("No pending products found.");
        } else {
            for (Product product : pendingProducts) {
                System.out.println(
                        product.getProductId() + " - " +
                                product.getProductName() + " - " +
                                product.getStatus());
            }
        }

        // 2. Test Approved Products
        System.out.println("\n===== APPROVED PRODUCTS =====");

        List<Product> approvedProducts = productDAO.getApprovedProducts();

        if (approvedProducts.isEmpty()) {
            System.out.println("No approved products found.");
        } else {
            for (Product product : approvedProducts) {
                System.out.println(
                        product.getProductId() + " - " +
                                product.getProductName() + " - " +
                                product.getStatus());
            }
        }

        // 3. Test Rejected Products
        System.out.println("\n===== REJECTED PRODUCTS =====");

        List<Product> rejectedProducts = productDAO.getRejectedProducts();

        if (rejectedProducts.isEmpty()) {
            System.out.println("No rejected products found.");
        } else {
            for (Product product : rejectedProducts) {
                System.out.println(
                        product.getProductId() + " - " +
                                product.getProductName() + " - " +
                                product.getStatus() + " - " +
                                product.getRejectionReason());
            }
        }

        System.out.println("\nAll Product Status Tests Completed!");
    }
}
