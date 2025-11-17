package com.example.demo.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordHashGenerator Tests")
class PasswordHashGeneratorTest {

    @Test
    @DisplayName("Should generate valid BCrypt hash")
    void testBCryptHashGeneration() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Test123!";
        
        String hash = encoder.encode(password);
        
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(hash.length() > 50);
        assertTrue(encoder.matches(password, hash));
    }

    @Test
    @DisplayName("Should generate different hashes for same password")
    void testDifferentHashesForSamePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Test123!";
        
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);
        
        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(password, hash1));
        assertTrue(encoder.matches(password, hash2));
    }

    @Test
    @DisplayName("Should verify password against hash")
    void testPasswordVerification() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Commercial123!";
        String hash = encoder.encode(password);
        
        assertTrue(encoder.matches(password, hash));
        assertFalse(encoder.matches("WrongPassword", hash));
    }

    @Test
    @DisplayName("Should handle multiple password encodings")
    void testMultiplePasswordEncodings() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] passwords = {"Test123!", "Commercial123!", "Admin@123"};
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            assertNotNull(hash);
            assertTrue(encoder.matches(password, hash));
        }
    }
}
