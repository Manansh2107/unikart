
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import junit.framework.TestCase;

public class SellerServiceTest extends TestCase {

    private AuthService authService;
    private SellerService sellerService;
    private SellerApplicationDAO applicationDAO;
    private String testEmail;
    private User customer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authService = new AuthService();
        sellerService = new SellerService();
        applicationDAO = new SellerApplicationDAO();

        testEmail = "seller_test_" + UUID.randomUUID() + "@unikart.com";

        customer = authService.registerCustomer(
                "Seller Test Customer",
                testEmail,
                "UnikartTest@2026",
                "9876543210"
        );
    }

    public void testCustomerCanApplyForSeller() throws SQLException {
        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "I want to sell products on Unikart."
        );

        assertTrue(applicationId > 0);

        Optional<SellerApplication> savedApplication =
                applicationDAO.findById(applicationId);

        assertTrue(savedApplication.isPresent());
        assertEquals(
                SellerApplication.Status.PENDING,
                savedApplication.get().getStatus()
        );
        assertEquals(
                customer.getUserId(),
                savedApplication.get().getUserId()
        );
    }

    public void testSellerApplicationIsSavedInDatabase() throws SQLException {
        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "Testing database storage."
        );

        Optional<SellerApplication> savedApplication =
                applicationDAO.findById(applicationId);

        assertTrue(savedApplication.isPresent());
        assertEquals(
                "Test College",
                savedApplication.get().getCollegeName()
        );
    }

    public void testSellerApplicationRequiresCollegeName() {
        try {
            sellerService.applyForSeller(
                    customer,
                    " ",
                    "Testing invalid application."
            );

            fail("Application should reject an empty college name.");

        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());

        } catch (SQLException e) {
            fail("Unexpected database error: " + e.getMessage());
        }
    }
}
