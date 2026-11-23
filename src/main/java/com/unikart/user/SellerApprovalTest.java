/*

 * Developer: MANANSH GOYAL
 * Development Date: 18-11-2026
 */
package com.unikart.user;

public class SellerApprovalTest {

    public static void main(String[] args) throws Exception {

        System.out.println("===== SELLER APPROVAL TEST =====");

        // Create an authenticated admin object for testing.
        User admin = new User();
        admin.setUserId(1);
        admin.setRole(User.Role.ADMIN);

        SellerService sellerService = new SellerService();

        int applicationId = 1;

        boolean approved = sellerService.approveApplication(
                admin,
                applicationId
        );

        if (approved) {
            System.out.println("Seller application approved successfully!");
        } else {
            System.out.println("Seller application approval failed!");
        }

        System.out.println("================================");
    }
}
