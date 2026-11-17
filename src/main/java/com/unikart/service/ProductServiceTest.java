
package com.unikart.service;

import com.unikart.product.Product;

public class ProductServiceTest {

    public static void main(String[] args) {

        ProductService productService = new ProductService();

        System.out.println("===== ALL PRODUCTS =====");

        for (Product product : productService.getAllProducts()) {
            System.out.println(
                    product.getProductId() + " - "
                            + product.getProductName() + " - "
                            + product.getStatus());
        }

        System.out.println("\n===== PENDING PRODUCTS =====");

        for (Product product : productService.getPendingProducts()) {
            System.out.println(
                    product.getProductId() + " - "
                            + product.getProductName());
        }

        System.out.println("\n===== APPROVED PRODUCTS =====");

        for (Product product : productService.getApprovedProducts()) {
            System.out.println(
                    product.getProductId() + " - "
                            + product.getProductName());
        }

        System.out.println("\n===== REJECTED PRODUCTS =====");

        for (Product product : productService.getRejectedProducts()) {
            System.out.println(
                    product.getProductId() + " - "
                            + product.getProductName());
        }

        System.out.println("\nProduct Service Tests Completed!");
    }
}
