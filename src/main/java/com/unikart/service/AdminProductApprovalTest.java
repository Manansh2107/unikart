/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.service;

import com.unikart.user.User;

public class AdminProductApprovalTest {

    public static void main(String[] args) {

        System.out.println("===== ADMIN PRODUCT APPROVAL TEST =====");

        User admin = new User();
        admin.setUserId(422);
        admin.setRole(User.Role.ADMIN);

        ProductService productService = new ProductService();

        boolean approved = productService.approveProduct(admin, 683);

        if (approved) {
            System.out.println("Seller product approved successfully!");
        } else {
            System.out.println("Product approval failed!");
        }

        System.out.println("=======================================");
    }
}
