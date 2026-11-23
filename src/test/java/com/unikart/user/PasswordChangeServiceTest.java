/*
 * Developers: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import junit.framework.TestCase;

public class PasswordChangeServiceTest extends TestCase {

    private AuthService authService;
    private PasswordChangeService passwordChangeService;
    private User customer;
    private String email;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authService = new AuthService();
        passwordChangeService = new PasswordChangeService();

        email = "password_test_" + UUID.randomUUID() + "@chitkara.edu.in";

        customer = authService.registerCustomer(
                "Password Test Customer",
                email,
                "OldPassword@2026",
                "9876543210"
        );
    }

    public void testPasswordChangesSuccessfully() throws SQLException {
        passwordChangeService.changePassword(
                customer,
                "OldPassword@2026",
                "NewPassword@2026"
        );

        Optional<User> updatedUser =
                new UserDAO().findById(customer.getUserId());

        assertTrue(updatedUser.isPresent());

        assertTrue(
                PasswordUtil.verifyPassword(
                        "NewPassword@2026",
                        updatedUser.get().getPasswordHash()
                )
        );
    }

    public void testIncorrectCurrentPasswordIsRejected() throws SQLException {
        try {
            passwordChangeService.changePassword(
                    customer,
                    "WrongPassword@2026",
                    "NewPassword@2026"
            );

            fail("Incorrect current password should be rejected.");

        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testOldPasswordNoLongerWorksAfterChange() throws SQLException {
        passwordChangeService.changePassword(
                customer,
                "OldPassword@2026",
                "NewPassword@2026"
        );

        assertFalse(
                authService.login(email, "OldPassword@2026").isPresent()
        );

        assertTrue(
                authService.login(email, "NewPassword@2026").isPresent()
        );
    }
}
