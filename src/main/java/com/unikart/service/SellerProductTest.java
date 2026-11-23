/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.service;

import com.unikart.product.Product;
import com.unikart.user.User;

import java.math.BigDecimal;

public class SellerProductTest {

    public static void main(String[] args) {

        System.out.println("===== SELLER PRODUCT TEST =====");

        User seller = new User();
        seller.setUserId(10);
        seller.setRole(User.Role.SELLER);

        Product product = new Product();
        product.setCategoryId(3);
        product.setProductName("Seller Test Notebook");
        product.setDescription("Notebook listed by an external seller for testing.");
        product.setPrice(new BigDecimal("150.00"));
        product.setStock(20);

        ProductService productService = new ProductService();

        boolean added = productService.addSellerProduct(seller, product);

        if (added) {
            System.out.println("Product submitted successfully!");
            System.out.println("Product Name: Seller Test Notebook");
            System.out.println("Seller ID: 10");
            System.out.println("Initial Status: PENDING");
        } else {
            System.out.println("Product submission failed!");
        }

        System.out.println("================================");
    }
}
