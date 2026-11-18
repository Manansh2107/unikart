package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import junit.framework.TestCase;

public class SellerServiceTest extends TestCase {

    private AuthService authService;
    private SellerService sellerService;
    private SellerApplicationDAO applicationDAO;
    private UserDAO userDAO;

    private String testEmail;
    private User customer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authService = new AuthService();
        sellerService = new SellerService();
        applicationDAO = new SellerApplicationDAO();
        userDAO = new UserDAO();

        testEmail = "seller_test_" + UUID.randomUUID()
                + "@chitkara.edu.in";

        customer = authService.registerCustomer(
                "Seller Test Customer",
                testEmail,
                "UnikartTest@2026",
                "9876543210"
        );
    }

    private User createAdmin() throws SQLException {
        String adminEmail = "admin_test_" + UUID.randomUUID()
                + "@chitkara.edu.in";

        User admin = new User(
                "Seller Test Admin",
                adminEmail,
                PasswordUtil.hashPassword("AdminTest@2026"),
                "9876543211",
                User.Role.ADMIN
        );

        int adminId = userDAO.createUser(admin);

        return userDAO.findById(adminId)
                .orElseThrow(() ->
                        new AssertionError("Test admin was not saved."));
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

    public void testSellerApplicationIsSavedInDatabase()
            throws SQLException {

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

    public void testAdminCanApproveSellerApplication()
            throws SQLException {

        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "Requesting seller approval."
        );

        User admin = createAdmin();

        sellerService.approveApplication(admin, applicationId);

        Optional<SellerApplication> savedApplication =
                applicationDAO.findById(applicationId);

        Optional<User> updatedCustomer =
                userDAO.findById(customer.getUserId());

        assertTrue(savedApplication.isPresent());
        assertTrue(updatedCustomer.isPresent());

        assertEquals(
                SellerApplication.Status.APPROVED,
                savedApplication.get().getStatus()
        );

        assertEquals(
                User.Role.SELLER,
                updatedCustomer.get().getRole()
        );
    }

    public void testAdminCanRejectSellerApplication()
            throws SQLException {

        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "Requesting seller approval."
        );

        User admin = createAdmin();
        String rejectionReason = "Incomplete application details.";

        sellerService.rejectApplication(
                admin,
                applicationId,
                rejectionReason
        );

        Optional<SellerApplication> savedApplication =
                applicationDAO.findById(applicationId);

        Optional<User> updatedCustomer =
                userDAO.findById(customer.getUserId());

        assertTrue(savedApplication.isPresent());
        assertTrue(updatedCustomer.isPresent());

        assertEquals(
                SellerApplication.Status.REJECTED,
                savedApplication.get().getStatus()
        );

        assertEquals(
                rejectionReason,
                savedApplication.get().getRejectionReason()
        );

        assertEquals(
                User.Role.CUSTOMER,
                updatedCustomer.get().getRole()
        );
    }

    public void testCustomerCannotApproveSellerApplication()
            throws SQLException {

        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "Requesting seller approval."
        );

        try {
            sellerService.approveApplication(customer, applicationId);

            fail("A customer must not be allowed to approve applications.");

        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testCustomerCannotRejectSellerApplication()
            throws SQLException {

        int applicationId = sellerService.applyForSeller(
                customer,
                "Test College",
                "Requesting seller approval."
        );

        try {
            sellerService.rejectApplication(
                    customer,
                    applicationId,
                    "Test rejection."
            );

            fail("A customer must not be allowed to reject applications.");

        } catch (SecurityException e) {
            assertNotNull(e.getMessage());
        }
    }
}