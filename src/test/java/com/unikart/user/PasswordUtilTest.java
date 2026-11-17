
package com.unikart.user;

import junit.framework.TestCase;

public class PasswordUtilTest extends TestCase {

    public void testPasswordHashAndVerification() {

        String password = "UnikartTest@2026";

        String hashedPassword =
                PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.equals(password));

        assertTrue(
                PasswordUtil.verifyPassword(
                        password, hashedPassword
                )
        );

        assertFalse(
                PasswordUtil.verifyPassword(
                        "WrongPassword@2026", hashedPassword
                )
        );
    }

    public void testSamePasswordProducesDifferentHashes() {

        String password = "UnikartTest@2026";

        String firstHash =
                PasswordUtil.hashPassword(password);

        String secondHash =
                PasswordUtil.hashPassword(password);

        assertFalse(firstHash.equals(secondHash));

        assertTrue(
                PasswordUtil.verifyPassword(password, firstHash)
        );

        assertTrue(
                PasswordUtil.verifyPassword(password, secondHash)
        );
    }

    public void testInvalidStoredHash() {

        assertFalse(
                PasswordUtil.verifyPassword(
                        "SomePassword@2026", "invalid-hash"
                )
        );
    }
}
