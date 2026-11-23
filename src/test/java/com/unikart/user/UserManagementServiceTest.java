/*
 * Developers: MANANSH GOYAL, Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import junit.framework.TestCase;

public class UserManagementServiceTest extends TestCase {

    private AuthService authService;
    private UserManagementService userManagementService;
    private User customer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authService = new AuthService();
        userManagementService = new UserManagementService();

        String email = "profile_test_" + UUID.randomUUID() + "@chitkara.edu.in";

        customer = authService.registerCustomer(
                "Original Name",
                email,
                "UnikartTest@2026",
                "9876543210"
        );
    }

    public void testGetUserProfile() throws SQLException {
        Optional<User> profileResult =
                userManagementService.getUserProfile(customer);

        assertTrue(profileResult.isPresent());

        User profile = profileResult.get();

        assertEquals(customer.getUserId(), profile.getUserId());
        assertEquals("Original Name", profile.getName());
        assertEquals(customer.getEmail(), profile.getEmail());
    }

    public void testUpdateUserProfile() throws SQLException {
        userManagementService.updateProfile(
                customer,
                "Updated Name",
                "9123456780"
        );

        Optional<User> updatedResult =
                userManagementService.getUserProfile(customer);

        assertTrue(updatedResult.isPresent());

        User updatedProfile = updatedResult.get();

        assertEquals("Updated Name", updatedProfile.getName());
        assertEquals("9123456780", updatedProfile.getPhone());
    }
}
