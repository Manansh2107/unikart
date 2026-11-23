/*

 * Developer: Himanshu
 * Development Date: 21-11-2026
 */
package com.unikart.seller;

import com.unikart.product.Category;
import com.unikart.product.CategoryDAO;
import com.unikart.product.Product;
import com.unikart.service.ProductService;
import com.unikart.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class SellerMenu {

    private final User seller;
    private final Scanner scanner;
    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public SellerMenu(User seller, Scanner scanner) {
        this.seller = seller;
        this.scanner = scanner;
    }

    public void showMenu() {

        boolean running = true;

        while (running) {

            System.out.println("\n========== SELLER PANEL ==========");
            System.out.println("1. Add New Product");
            System.out.println("2. View My Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. View Approval Status");
            System.out.println("6. Resubmit Rejected Product");
            System.out.println("7. Logout");
            System.out.println("==================================");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addProduct();
                    break;

                case "2":
                    viewProducts();
                    break;

                case "3":
                    updateProduct();
                    break;

                case "4":
                    deleteProduct();
                    break;

                case "5":
                    viewApprovalStatus();
                    break;

                case "6":
                    resubmitProduct();
                    break;

                case "7":
                    running = false;
                    System.out.println("Seller logged out successfully.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addProduct() {

        try {
            System.out.println("\n========== ADD NEW PRODUCT ==========");

            List<Category> categories = categoryDAO.getAllCategories();

            if (categories.isEmpty()) {
                System.out.println("No categories available.");
                return;
            }

            System.out.println("\nAvailable Categories:");

            for (Category category : categories) {
                System.out.println(
                        category.getCategoryId() + ". " +
                                category.getCategoryName());
            }

            System.out.print("Enter Category ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter Price: ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Enter Stock: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());

            Product product = new Product();

            product.setCategoryId(categoryId);
            product.setProductName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);

            boolean added = productService.addSellerProduct(seller, product);

            if (added) {
                System.out.println("\nProduct submitted successfully!");
                System.out.println("Your product is awaiting admin approval.");
            } else {
                System.out.println("\nProduct submission failed.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    private void viewProducts() {

        try {
            System.out.println("\n========== MY PRODUCTS ==========");

            List<Product> products = productService.getProductsBySellerId(seller);

            if (products.isEmpty()) {
                System.out.println("You have not added any products.");
                return;
            }

            for (Product product : products) {

                System.out.println("----------------------------------");
                System.out.println("Product ID: " + product.getProductId());
                System.out.println("Name: " + product.getProductName());
                System.out.println("Description: " + product.getDescription());
                System.out.println("Price: Rs. " + product.getPrice());
                System.out.println("Stock: " + product.getStock());
                System.out.println("Status: " + product.getStatus());
            }

            System.out.println("----------------------------------");

        } catch (Exception e) {
            System.out.println("Error retrieving products: " + e.getMessage());
        }
    }

    private void updateProduct() {

        try {
            System.out.println("\n========== UPDATE PRODUCT ==========");

            List<Product> products = productService.getProductsBySellerId(seller);

            if (products.isEmpty()) {
                System.out.println("You have no products to update.");
                return;
            }

            for (Product product : products) {
                System.out.println(
                        "ID: " + product.getProductId() +
                                " | Name: " + product.getProductName());
            }

            System.out.print("Enter Product ID to update: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            Product existing = null;

            for (Product product : products) {
                if (product.getProductId() == productId) {
                    existing = product;
                    break;
                }
            }

            if (existing == null) {
                System.out.println("Product not found in your listings.");
                return;
            }

            System.out.println("\nAvailable Categories:");

            List<Category> categories = categoryDAO.getAllCategories();

            for (Category category : categories) {
                System.out.println(
                        category.getCategoryId() + ". " +
                                category.getCategoryName());
            }

            System.out.print("Enter New Category ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter New Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter New Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter New Price: ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Enter New Stock: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());

            Product updatedProduct = new Product();

            updatedProduct.setProductId(productId);
            updatedProduct.setCategoryId(categoryId);
            updatedProduct.setProductName(name);
            updatedProduct.setDescription(description);
            updatedProduct.setPrice(price);
            updatedProduct.setStock(stock);

            boolean updated = productService.updateProduct(seller, updatedProduct);

            if (updated) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Product update failed.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");

        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }

    private void deleteProduct() {

        try {
            System.out.println("\n========== DELETE PRODUCT ==========");

            List<Product> products = productService.getProductsBySellerId(seller);

            if (products.isEmpty()) {
                System.out.println("You have no products to delete.");
                return;
            }

            for (Product product : products) {
                System.out.println(
                        "ID: " + product.getProductId() +
                                " | Name: " + product.getProductName());
            }

            System.out.print("Enter Product ID to delete: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            boolean deleted = productService.deleteProduct(seller, productId);

            if (deleted) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Product deletion failed.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID.");

        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }

    private void viewApprovalStatus() {

        try {
            System.out.println("\n========== PRODUCT APPROVAL STATUS ==========");

            List<Product> products = productService.getProductsBySellerId(seller);

            if (products.isEmpty()) {
                System.out.println("You have not added any products.");
                return;
            }

            for (Product product : products) {

                System.out.println("----------------------------------");
                System.out.println("Product ID: " + product.getProductId());
                System.out.println("Product Name: " + product.getProductName());
                System.out.println("Status: " + product.getStatus());

                if ("REJECTED".equalsIgnoreCase(product.getStatus())) {
                    System.out.println(
                            "Rejection Reason: " +
                                    product.getRejectionReason());
                }
            }

            System.out.println("----------------------------------");

        } catch (Exception e) {
            System.out.println("Error retrieving approval status: " +
                    e.getMessage());
        }
    }

    private void resubmitProduct() {

        try {
            System.out.println("\n========== RESUBMIT PRODUCT ==========");

            List<Product> products = productService.getProductsBySellerId(seller);

            boolean foundRejected = false;

            for (Product product : products) {

                if ("REJECTED".equalsIgnoreCase(product.getStatus())) {

                    foundRejected = true;

                    System.out.println(
                            "ID: " + product.getProductId() +
                                    " | Name: " + product.getProductName());
                }
            }

            if (!foundRejected) {
                System.out.println("You have no rejected products.");
                return;
            }

            System.out.print("Enter Product ID to resubmit: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            boolean resubmitted = productService.resubmitProduct(seller, productId);

            if (resubmitted) {
                System.out.println("Product resubmitted successfully!");
                System.out.println("It is now awaiting admin approval.");
            } else {
                System.out.println("Product resubmission failed.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID.");

        } catch (Exception e) {
            System.out.println("Error resubmitting product: " +
                    e.getMessage());
        }
    }
}
