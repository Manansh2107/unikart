/*
 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class AuthServiceTest extends TestCase {

    private AuthService authService;
    private String testEmail;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authService = new AuthService();

        testEmail = "test_"
                + UUID.randomUUID()
                + "@chitkara.edu.in";
    }

    public void testCustomerRegistration() throws SQLException {

        User registeredUser = authService.registerCustomer(
                "Test Customer",
                testEmail,
                "UnikartTest@2026",
                "9876543210"
        );

        assertNotNull(registeredUser);
        assertTrue(registeredUser.getUserId() > 0);
        assertEquals(testEmail, registeredUser.getEmail());
        assertEquals(User.Role.CUSTOMER, registeredUser.getRole());

        // Password must never be stored as plain text.
        assertFalse(
                "UnikartTest@2026".equals(
                        registeredUser.getPasswordHash()
                )
        );
    }

    public void testLoginWithCorrectPassword() throws SQLException {

        authService.registerCustomer(
                "Login Test",
                testEmail,
                "UnikartTest@2026",
                null
        );

        Optional<User> result = authService.login(
                testEmail,
                "UnikartTest@2026"
        );

        assertTrue(result.isPresent());
    }

    public void testLoginWithIncorrectPassword() throws SQLException {

        authService.registerCustomer(
                "Wrong Password Test",
                testEmail,
                "UnikartTest@2026",
                null
        );

        Optional<User> result = authService.login(
                testEmail,
                "WrongPassword@2026"
        );

        assertFalse(result.isPresent());
    }

    public void testDuplicateEmailRegistration() throws SQLException {

        authService.registerCustomer(
                "First Customer",
                testEmail,
                "UnikartTest@2026",
                null
        );

        try {
            authService.registerCustomer(
                    "Second Customer",
                    testEmail,
                    "AnotherPassword@2026",
                    null
            );

            fail("Duplicate email registration should be rejected.");

        } catch (IllegalArgumentException e) {
            assertEquals(
                    "An account with this email already exists.",
                    e.getMessage()
            );
        }
    }
}
