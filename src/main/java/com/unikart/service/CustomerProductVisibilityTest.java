/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.service;

import com.unikart.product.Product;
import java.util.List;

public class CustomerProductVisibilityTest {

    public static void main(String[] args) {

        System.out.println("===== CUSTOMER PRODUCT VISIBILITY TEST =====");

        ProductService productService = new ProductService();

        List<Product> products = productService.getApprovedProducts();

        boolean found = false;

        for (Product product : products) {

            if (product.getProductId() == 683) {

                found = true;

                System.out.println("External seller product is visible!");
                System.out.println("Product ID: " + product.getProductId());
                System.out.println("Product Name: " + product.getProductName());
                System.out.println("Seller ID: " + product.getSellerId());
                System.out.println("Price: " + product.getPrice());
                System.out.println("Status: " + product.getStatus());

                break;
            }
        }

        if (!found) {
            System.out.println("Product 683 was not found!");
        }

        System.out.println("============================================");
    }
}
