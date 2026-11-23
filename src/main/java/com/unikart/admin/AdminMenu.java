/*

 * Developer: MANANSH GOYAL
 * Development Dates: 18-11-2026 and 22-11-2026
 */
package com.unikart.admin;

import com.unikart.product.Category;
import com.unikart.product.CategoryDAO;
import com.unikart.product.Product;
import com.unikart.product.ProductDAO;
import com.unikart.service.ProductService;
import com.unikart.user.SellerApplication;
import com.unikart.user.SellerService;
import com.unikart.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final User admin;
    private final Scanner scanner;
    private final ProductService productService;
    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO;
    private final SellerService sellerService;

    public AdminMenu(User admin, Scanner scanner) {
        this.admin = admin;
        this.scanner = scanner;
        this.productService = new ProductService();
        this.categoryDAO = new CategoryDAO();
        this.productDAO = new ProductDAO();
        this.sellerService = new SellerService();
    }

    // MAIN ADMIN MENU

    public void showMenu() {

        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            System.out.println("Access denied! Admin privileges required.");
            return;
        }

        while (true) {

            System.out.println("\n====================================");
            System.out.println("          UNIKART ADMIN PANEL");
            System.out.println("====================================");
            System.out.println("Welcome, " + admin.getName());
            System.out.println("------------------------------------");
            System.out.println("1. Add Unikart Product");
            System.out.println("2. View All Products");
            System.out.println("3. Update Unikart Product");
            System.out.println("4. Delete Unikart Product");
            System.out.println("5. View Pending Products");
            System.out.println("6. Approve Product");
            System.out.println("7. Reject Product");
            System.out.println("8. View Rejected Products");
            System.out.println("9. View Pending Seller Applications");
            System.out.println("10. Approve Seller Application");
            System.out.println("11. Reject Seller Application");
            System.out.println("12. Logout");
            System.out.println("------------------------------------");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    addProduct();
                    break;

                case "2":
                    viewAllProducts();
                    break;

                case "3":
                    updateProduct();
                    break;

                case "4":
                    deleteProduct();
                    break;

                case "5":
                    viewPendingProducts();
                    break;

                case "6":
                    approveProduct();
                    break;

                case "7":
                    rejectProduct();
                    break;

                case "8":
                    viewRejectedProducts();
                    break;

                case "9":
                    viewPendingSellerApplications();
                    break;

                case "10":
                    approveSellerApplication();
                    break;

                case "11":
                    rejectSellerApplication();
                    break;

                case "12":
                    System.out.println("\nAdmin logged out successfully.");
                    return;

                default:
                    System.out.println("\nInvalid choice! Please try again.");
            }
        }
    }

    // ADD UNIKART PRODUCT

    private void addProduct() {

        System.out.println("\n========== ADD UNIKART PRODUCT ==========");

        try {

            List<Category> categories = categoryDAO.getAllCategories();

            if (categories.isEmpty()) {
                System.out.println("No categories available.");
                return;
            }

            System.out.println("\nAvailable Categories:");

            for (Category category : categories) {
                System.out.println(
                        category.getCategoryId() + ". "
                                + category.getCategoryName()
                );
            }

            System.out.print("\nEnter Category ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine().trim());

            if (!categoryExists(categories, categoryId)) {
                System.out.println("Invalid category ID!");
                return;
            }

            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Product Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter Product Price: ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Enter Available Stock: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());

            Product product = new Product();

            product.setCategoryId(categoryId);
            product.setProductName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);

            boolean added = productService.addAdminProduct(admin, product);

            if (added) {
                System.out.println("\nProduct added successfully!");
                System.out.println("Product Status: PENDING");
                System.out.println("Owner: Unikart");
            } else {
                System.out.println("\nUnable to add product.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered. Please try again.");

        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
    }

    // VIEW ALL PRODUCTS

    private void viewAllProducts() {

        System.out.println("\n================ ALL PRODUCTS ================");

        try {

            List<Product> products = productService.getAllProducts(admin);

            displayProducts(products);

        } catch (Exception e) {
            System.out.println("Unable to fetch products: " + e.getMessage());
        }
    }

    // UPDATE UNIKART PRODUCT

    private void updateProduct() {

        System.out.println("\n========== UPDATE UNIKART PRODUCT ==========");

        try {

            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            Product existingProduct = productDAO.getProductById(productId);

            if (existingProduct == null) {
                System.out.println("Product not found.");
                return;
            }

            if (existingProduct.getSellerId() != null) {
                System.out.println("This product belongs to an external seller.");
                System.out.println("You can only update Unikart-owned products.");
                return;
            }

            System.out.println("\nCurrent Product Details:");
            System.out.println("Name: " + existingProduct.getProductName());
            System.out.println("Description: " + existingProduct.getDescription());
            System.out.println("Price: " + existingProduct.getPrice());
            System.out.println("Stock: " + existingProduct.getStock());
            System.out.println("Category ID: " + existingProduct.getCategoryId());

            List<Category> categories = categoryDAO.getAllCategories();

            if (categories.isEmpty()) {
                System.out.println("No categories available.");
                return;
            }

            System.out.println("\nAvailable Categories:");

            for (Category category : categories) {
                System.out.println(
                        category.getCategoryId() + ". "
                                + category.getCategoryName()
                );
            }

            System.out.print("\nEnter New Category ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine().trim());

            if (!categoryExists(categories, categoryId)) {
                System.out.println("Invalid category ID.");
                return;
            }

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
            updatedProduct.setSellerId(null);
            updatedProduct.setCategoryId(categoryId);
            updatedProduct.setProductName(name);
            updatedProduct.setDescription(description);
            updatedProduct.setPrice(price);
            updatedProduct.setStock(stock);

            boolean updated = productService.updateAdminProduct(
                    admin,
                    updatedProduct
            );

            if (updated) {
                System.out.println("\nProduct updated successfully!");
                System.out.println(
                        "If product details changed, its status may return to PENDING."
                );
            } else {
                System.out.println("\nUnable to update product.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");

        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }

    // DELETE UNIKART PRODUCT

    private void deleteProduct() {

        System.out.println("\n========== DELETE UNIKART PRODUCT ==========");

        try {

            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            Product existingProduct = productDAO.getProductById(productId);

            if (existingProduct == null) {
                System.out.println("Product not found.");
                return;
            }

            if (existingProduct.getSellerId() != null) {
                System.out.println("This product belongs to an external seller.");
                System.out.println("You can only delete Unikart-owned products.");
                return;
            }

            System.out.println("\nProduct Details:");
            System.out.println("Name: " + existingProduct.getProductName());
            System.out.println("Price: " + existingProduct.getPrice());
            System.out.println("Stock: " + existingProduct.getStock());

            System.out.print(
                    "\nAre you sure you want to delete this product? (Y/N): "
            );

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Product deletion cancelled.");
                return;
            }

            boolean deleted = productService.deleteAdminProduct(
                    admin,
                    productId
            );

            if (deleted) {
                System.out.println("\nProduct deleted successfully!");
            } else {
                System.out.println("\nUnable to delete product.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID.");

        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }

    // VIEW PENDING PRODUCTS

    private void viewPendingProducts() {

        System.out.println("\n========== PENDING PRODUCTS ==========");

        try {

            List<Product> products = productService.getPendingProducts(admin);

            if (products.isEmpty()) {
                System.out.println("No pending products found.");
                return;
            }

            displayProducts(products);

        } catch (Exception e) {
            System.out.println(
                    "Unable to fetch pending products: " + e.getMessage()
            );
        }
    }

    // APPROVE PRODUCT

    private void approveProduct() {

        System.out.println("\n========== APPROVE PRODUCT ==========");

        try {

            System.out.print("Enter Product ID to approve: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            boolean approved = productService.approveProduct(admin, productId);

            if (approved) {
                System.out.println("\nProduct approved successfully!");
                System.out.println("Product Status: APPROVED");
            } else {
                System.out.println(
                        "\nProduct could not be approved. Check whether it exists and is PENDING."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID.");

        } catch (Exception e) {
            System.out.println("Error approving product: " + e.getMessage());
        }
    }

    // REJECT PRODUCT

    private void rejectProduct() {

        System.out.println("\n========== REJECT PRODUCT ==========");

        try {

            System.out.print("Enter Product ID to reject: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            Product product = productDAO.getProductById(productId);

            if (product == null) {
                System.out.println("Product not found.");
                return;
            }

            if (!"PENDING".equalsIgnoreCase(product.getStatus())) {
                System.out.println("Only pending products can be rejected.");
                return;
            }

            System.out.println("\nProduct Name: " + product.getProductName());
            System.out.println("Product Price: " + product.getPrice());

            System.out.print("\nEnter Reason for Rejection: ");
            String reason = scanner.nextLine().trim();

            if (reason.isEmpty()) {
                System.out.println("Rejection reason cannot be empty.");
                return;
            }

            boolean rejected = productService.rejectProduct(
                    admin,
                    productId,
                    reason
            );

            if (rejected) {
                System.out.println("\nProduct rejected successfully!");
                System.out.println("Product Status: REJECTED");
            } else {
                System.out.println("\nUnable to reject product.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID.");

        } catch (Exception e) {
            System.out.println("Error rejecting product: " + e.getMessage());
        }
    }

    // VIEW REJECTED PRODUCTS

    private void viewRejectedProducts() {

        System.out.println("\n========== REJECTED PRODUCTS ==========");

        try {

            List<Product> products = productService.getRejectedProducts(admin);

            if (products.isEmpty()) {
                System.out.println("No rejected products found.");
                return;
            }

            displayProducts(products);

            System.out.println("\nRejection Details:");

            for (Product product : products) {

                System.out.println("\nProduct ID: " + product.getProductId());
                System.out.println("Product Name: " + product.getProductName());
                System.out.println(
                        "Rejection Reason: " + product.getRejectionReason()
                );
            }

        } catch (Exception e) {
            System.out.println(
                    "Unable to fetch rejected products: " + e.getMessage()
            );
        }
    }

    // VIEW PENDING SELLER APPLICATIONS

    private void viewPendingSellerApplications() {

        System.out.println("\n========== PENDING SELLER APPLICATIONS ==========");

        try {

            List<SellerApplication> applications =
                    sellerService.getPendingApplications(admin);

            if (applications == null || applications.isEmpty()) {
                System.out.println("No pending seller applications found.");
                return;
            }

            for (SellerApplication application : applications) {

                System.out.println("------------------------------------");
                System.out.println(
                        "Application ID: " + application.getApplicationId()
                );
                System.out.println("User ID: " + application.getUserId());
                System.out.println(
                        "College: " + application.getCollegeName()
                );
                System.out.println(
                        "Reason: " + application.getReason()
                );
                System.out.println(
                        "Applied At: " + application.getAppliedAt()
                );
                System.out.println("Status: " + application.getStatus());
            }

            System.out.println("------------------------------------");
            System.out.println(
                    "Total Pending Applications: " + applications.size()
            );

        } catch (Exception e) {
            System.out.println(
                    "Unable to fetch seller applications: " + e.getMessage()
            );
        }
    }

    // APPROVE SELLER APPLICATION

    private void approveSellerApplication() {

        System.out.println("\n========== APPROVE SELLER APPLICATION ==========");

        try {

            System.out.print("Enter Application ID to approve: ");
            int applicationId = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            System.out.print(
                    "Are you sure you want to approve this application? (Y/N): "
            );

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Application approval cancelled.");
                return;
            }

            boolean approved = sellerService.approveApplication(
                    admin,
                    applicationId
            );

            if (approved) {
                System.out.println("\nSeller application approved successfully!");
                System.out.println("The customer is now a SELLER.");
            } else {
                System.out.println(
                        "\nApplication could not be approved. "
                                + "Check whether it exists and is PENDING."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID.");

        } catch (Exception e) {
            System.out.println(
                    "Error approving seller application: " + e.getMessage()
            );
        }
    }

    // REJECT SELLER APPLICATION

    private void rejectSellerApplication() {

        System.out.println("\n========== REJECT SELLER APPLICATION ==========");

        try {

            System.out.print("Enter Application ID to reject: ");
            int applicationId = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            System.out.print("Enter Reason for Rejection: ");
            String reason = scanner.nextLine().trim();

            if (reason.isEmpty()) {
                System.out.println("Rejection reason cannot be empty.");
                return;
            }

            boolean rejected = sellerService.rejectApplication(
                    admin,
                    applicationId,
                    reason
            );

            if (rejected) {
                System.out.println("\nSeller application rejected successfully!");
                System.out.println("Status: REJECTED");
                System.out.println("Reason: " + reason);
            } else {
                System.out.println(
                        "\nApplication could not be rejected. "
                                + "Check whether it exists and is PENDING."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid Application ID.");

        } catch (Exception e) {
            System.out.println(
                    "Error rejecting seller application: " + e.getMessage()
            );
        }
    }

    // COMMON PRODUCT DISPLAY METHOD

    private void displayProducts(List<Product> products) {

        if (products == null || products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.printf(
                "%-6s %-22s %-10s %-12s %-8s %-15s %-12s%n",
                "ID",
                "PRODUCT",
                "CATEGORY",
                "PRICE",
                "STOCK",
                "OWNER",
                "STATUS"
        );

        System.out.println(
                "--------------------------------------------------------------------------------"
        );

        for (Product product : products) {

            String owner;

            if (product.getSellerId() == null) {
                owner = "Unikart";
            } else {
                owner = "Seller ID: " + product.getSellerId();
            }

            System.out.printf(
                    "%-6d %-22s %-10d %-12s %-8d %-15s %-12s%n",
                    product.getProductId(),
                    product.getProductName(),
                    product.getCategoryId(),
                    product.getPrice(),
                    product.getStock(),
                    owner,
                    product.getStatus()
            );
        }

        System.out.println(
                "--------------------------------------------------------------------------------"
        );

        System.out.println("Total Products: " + products.size());
    }

    // CHECK WHETHER CATEGORY EXISTS

    private boolean categoryExists(
            List<Category> categories,
            int categoryId
    ) {

        for (Category category : categories) {

            if (category.getCategoryId() == categoryId) {
                return true;
            }
        }

        return false;
    }
}
