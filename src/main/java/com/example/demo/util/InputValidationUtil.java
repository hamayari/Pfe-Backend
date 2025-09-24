package com.example.demo.util;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization.
 * Helps prevent XSS, SQL injection, and other common security vulnerabilities.
 */
@Component
public class InputValidationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(InputValidationUtil.class);
    
    // Common regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_\\.-]{3,50}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    );

    /**
     * Sanitize HTML input to prevent XSS attacks
     * @param input The input string to sanitize
     * @return Sanitized string with HTML/JS removed
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return Jsoup.clean(input, Safelist.basic());
    }
    
    /**
     * Validate and sanitize email address
     * @param email The email to validate
     * @return Sanitized email or null if invalid
     */
    public String validateAndSanitizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String sanitized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(sanitized).matches()) {
            logger.warn("Invalid email format: {}", email);
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * Validate and sanitize username
     * @param username The username to validate
     * @return Sanitized username or null if invalid
     */
    public String validateAndSanitizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        String sanitized = username.trim();
        if (!USERNAME_PATTERN.matcher(sanitized).matches()) {
            logger.warn("Invalid username format: {}", username);
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * Validate password strength
     * @param password The password to validate
     * @return true if password meets security requirements
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Prevent SQL injection by escaping special characters
     * @param input The input to escape
     * @return Escaped string safe for SQL queries
     */
    public String escapeSql(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeJava(input);
    }
    
    /**
     * Check if input contains any potentially dangerous content
     * @param input The input to check
     * @return true if input is safe, false if potentially dangerous
     */
    public boolean isSafeInput(String input) {
        if (input == null) {
            return true;
        }
        // Check for common XSS patterns
        String lowerInput = input.toLowerCase();
        return !(lowerInput.contains("<script>") || 
                lowerInput.contains("</script>") ||
                lowerInput.contains("javascript:") ||
                lowerInput.contains("onerror=") ||
                lowerInput.contains("onload=") ||
                lowerInput.contains("eval("));
    }
}
