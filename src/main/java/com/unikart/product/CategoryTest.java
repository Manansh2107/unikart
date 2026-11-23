/*

 * Developer: Himanshu
 * Development Date: 19-11-2026
 */
package com.unikart.product;

import java.util.List;

public class CategoryTest {

    public static void main(String[] args) {

        CategoryDAO categoryDAO = new CategoryDAO();

        System.out.println("\n===== ALL CATEGORIES =====");

        List<Category> categories = categoryDAO.getAllCategories();

        if (categories.isEmpty()) {
            System.out.println("No categories found.");
        } else {
            for (Category category : categories) {
                System.out.println(
                        category.getCategoryId() + " - " +
                                category.getCategoryName() + " - " +
                                category.getDescription());
            }
        }

        System.out.println("\nTotal Categories: " + categories.size());
        System.out.println("\nCategory Retrieval Test Completed!");
    }
}
