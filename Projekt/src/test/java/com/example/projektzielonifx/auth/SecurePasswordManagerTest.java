package com.example.projektzielonifx.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prosty test klasy SecurePasswordManager.
 * Sprawdza, czy haszowanie i weryfikacja hasła działają poprawnie.
 */
public class SecurePasswordManagerTest {

    @Test
    public void testHashAndVerifyPassword() {
        String plainPassword = "mojeHaslo123";

        // Haszujemy hasło
        String hashedPassword = SecurePasswordManager.hashPassword(plainPassword);

        // Hasło po haszowaniu nie powinno być puste ani równe oryginałowi
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(plainPassword, hashedPassword, "Hashed password should differ from plain password");

        // Weryfikacja prawidłowego hasła powinna zwrócić true
        assertTrue(SecurePasswordManager.verifyPassword(plainPassword, hashedPassword),
                "Password verification should return true for correct password");

        // Weryfikacja błędnego hasła powinna zwrócić false
        assertFalse(SecurePasswordManager.verifyPassword("zleHaslo", hashedPassword),
                "Password verification should return false for incorrect password");

        System.out.println("Test SecurePasswordManager wykonany poprawnie.");
    }
}
