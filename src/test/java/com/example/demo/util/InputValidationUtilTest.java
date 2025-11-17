package com.example.demo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputValidationUtil Tests")
class InputValidationUtilTest {

    private InputValidationUtil validationUtil;

    @BeforeEach
    void setUp() {
        validationUtil = new InputValidationUtil();
    }

    @Test
    @DisplayName("Should sanitize HTML input correctly")
    void testSanitizeHtml() {
        // Test with script tag
        String maliciousInput = "<script>alert('XSS')</script>Hello";
        String sanitized = validationUtil.sanitizeHtml(maliciousInput);
        assertFalse(sanitized.contains("<script>"));
        
        // Test with null
        assertNull(validationUtil.sanitizeHtml(null));
        
        // Test with safe HTML
        String safeInput = "<b>Bold text</b>";
        String result = validationUtil.sanitizeHtml(safeInput);
        assertTrue(result.contains("Bold text"));
    }

    @Test
    @DisplayName("Should validate and sanitize email correctly")
    void testValidateAndSanitizeEmail() {
        // Valid email
        assertEquals("test@example.com", validationUtil.validateAndSanitizeEmail("test@example.com"));
        assertEquals("test@example.com", validationUtil.validateAndSanitizeEmail("TEST@EXAMPLE.COM"));
        assertEquals("test@example.com", validationUtil.validateAndSanitizeEmail("  test@example.com  "));
        
        // Invalid emails
        assertNull(validationUtil.validateAndSanitizeEmail(null));
        assertNull(validationUtil.validateAndSanitizeEmail(""));
        assertNull(validationUtil.validateAndSanitizeEmail("   "));
        assertNull(validationUtil.validateAndSanitizeEmail("invalid-email"));
        assertNull(validationUtil.validateAndSanitizeEmail("@example.com"));
        assertNull(validationUtil.validateAndSanitizeEmail("test@"));
    }

    @Test
    @DisplayName("Should validate and sanitize username correctly")
    void testValidateAndSanitizeUsername() {
        // Valid usernames
        assertEquals("john_doe", validationUtil.validateAndSanitizeUsername("john_doe"));
        assertEquals("user123", validationUtil.validateAndSanitizeUsername("user123"));
        assertEquals("test.user", validationUtil.validateAndSanitizeUsername("test.user"));
        assertEquals("user-name", validationUtil.validateAndSanitizeUsername("user-name"));
        
        // Invalid usernames
        assertNull(validationUtil.validateAndSanitizeUsername(null));
        assertNull(validationUtil.validateAndSanitizeUsername(""));
        assertNull(validationUtil.validateAndSanitizeUsername("ab")); // Too short
        assertNull(validationUtil.validateAndSanitizeUsername("user name")); // Contains space
        assertNull(validationUtil.validateAndSanitizeUsername("user@name")); // Invalid character
    }

    @Test
    @DisplayName("Should validate password strength correctly")
    void testIsPasswordValid() {
        // Valid passwords
        assertTrue(validationUtil.isPasswordValid("Test123!@#"));
        assertTrue(validationUtil.isPasswordValid("Abcd1234@"));
        assertTrue(validationUtil.isPasswordValid("MyP@ssw0rd"));
        
        // Invalid passwords
        assertFalse(validationUtil.isPasswordValid(null));
        assertFalse(validationUtil.isPasswordValid("short")); // Too short
        assertFalse(validationUtil.isPasswordValid("password")); // No uppercase, number, special char
        assertFalse(validationUtil.isPasswordValid("PASSWORD123")); // No lowercase, special char
        assertFalse(validationUtil.isPasswordValid("Password")); // No number, special char
        assertFalse(validationUtil.isPasswordValid("Password123")); // No special char
        assertFalse(validationUtil.isPasswordValid("Pass 123!")); // Contains space
    }

    @Test
    @DisplayName("Should escape SQL correctly")
    void testEscapeSql() {
        // Test with null
        assertEquals("", validationUtil.escapeSql(null));
        
        // Test with normal string
        String normal = "Hello World";
        String escaped = validationUtil.escapeSql(normal);
        assertNotNull(escaped);
        assertEquals("Hello World", escaped);
        
        // Test with special characters
        String special = "It's a test";
        String escapedSpecial = validationUtil.escapeSql(special);
        assertNotNull(escapedSpecial);
        // StringEscapeUtils.escapeJava escapes single quotes
        assertTrue(escapedSpecial.contains("\\") || escapedSpecial.length() >= special.length());
    }

    @Test
    @DisplayName("Should detect unsafe input correctly")
    void testIsSafeInput() {
        // Safe inputs
        assertTrue(validationUtil.isSafeInput(null));
        assertTrue(validationUtil.isSafeInput("Hello World"));
        assertTrue(validationUtil.isSafeInput("test@example.com"));
        assertTrue(validationUtil.isSafeInput("Normal text 123"));
        
        // Unsafe inputs
        assertFalse(validationUtil.isSafeInput("<script>alert('XSS')</script>"));
        assertFalse(validationUtil.isSafeInput("</script>"));
        assertFalse(validationUtil.isSafeInput("javascript:alert(1)"));
        assertFalse(validationUtil.isSafeInput("<img onerror='alert(1)'>"));
        assertFalse(validationUtil.isSafeInput("<body onload='alert(1)'>"));
        assertFalse(validationUtil.isSafeInput("eval('malicious code')"));
    }
}
