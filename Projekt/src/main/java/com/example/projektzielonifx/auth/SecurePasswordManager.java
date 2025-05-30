package com.example.projektzielonifx.auth;

import org.mindrot.jbcrypt.BCrypt;

public class SecurePasswordManager {
    /**
     * Hash a plain text password using bcrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify a plain text password against a stored hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
