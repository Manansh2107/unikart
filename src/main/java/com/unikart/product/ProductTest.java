
package com.unikart.product;

import java.math.BigDecimal;
import java.util.List;

public class ProductTest {

    public static void main(String[] args) {

        ProductDAO productDAO = new ProductDAO();

        // 1. Create a test product
        Product product = new Product();

        product.setSellerId(null);
        product.setCategoryId(1);
        product.setProductName("Admin Workflow Test");
        product.setDescription("Testing ProductDAO features");
        product.setPrice(new BigDecimal("99.00"));
        product.setStock(10);

        boolean added = productDAO.addProduct(product);

        if (!added) {
            System.out.println("Product creation failed!");
            return;
        }

        System.out.println("Product added successfully!");

        // Get newly added product ID
        List<Product> products = productDAO.getAllProducts();

        int productId = products.get(0).getProductId();

        System.out.println("Test Product ID: " + productId);

        // 2. Test approval
        System.out.println("\nApprove Product:");

        boolean approved = productDAO.approveProduct(productId);

        System.out.println(approved ? "Approved successfully!" : "Approval failed!");

        Product p = productDAO.getProductById(productId);

        System.out.println("Status: " + p.getStatus());

        // 3. Test rejection
        System.out.println("\nReject Product:");

        boolean rejected = productDAO.rejectProduct(
                productId,
                "Product information needs correction");

        System.out.println(rejected ? "Rejected successfully!" : "Rejection failed!");

        p = productDAO.getProductById(productId);

        System.out.println("Status: " + p.getStatus());
        System.out.println("Reason: " + p.getRejectionReason());

        // 4. Test resubmission
        System.out.println("\nResubmit Product:");

        boolean resubmitted = productDAO.resubmitProduct(productId);

        System.out.println(resubmitted ? "Resubmitted successfully!" : "Resubmission failed!");

        p = productDAO.getProductById(productId);

        System.out.println("Status: " + p.getStatus());
        System.out.println("Rejection Reason: " + p.getRejectionReason());

        // 5. Approve again for search and filtering tests
        System.out.println("\nApprove Product Again:");

        boolean approvedAgain = productDAO.approveProduct(productId);

        System.out.println(approvedAgain ? "Approved successfully!" : "Approval failed!");

        // 6. Test product search
        System.out.println("\nSearch Products:");

        List<Product> searchResults = productDAO.searchProducts("Admin Workflow");

        if (searchResults.isEmpty()) {
            System.out.println("No matching products found!");
        } else {
            for (Product item : searchResults) {
                System.out.println(
                        item.getProductId() + " - " +
                                item.getProductName());
            }
        }

        // 7. Test category filtering
        System.out.println("\nProducts By Category:");

        List<Product> categoryResults = productDAO.getProductsByCategory(1);

        if (categoryResults.isEmpty()) {
            System.out.println("No products found in this category!");
        } else {
            for (Product item : categoryResults) {
                System.out.println(
                        item.getProductName() + " - " +
                                item.getPrice());
            }
        }

        // 8. Test price sorting
        System.out.println("\nProducts Sorted By Price:");

        List<Product> sortedProducts = productDAO.sortProductsByPrice("ASC");

        if (sortedProducts.isEmpty()) {
            System.out.println("No approved products available!");
        } else {
            for (Product item : sortedProducts) {
                System.out.println(
                        item.getProductName() + " - " +
                                item.getPrice());
            }
        }

        System.out.println("\nAll ProductDAO tests completed!");
    }
}
