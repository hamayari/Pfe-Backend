package com.example.demo.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtUtils Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb25UZXN0";
    private static final int TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", TEST_EXPIRATION * 2);
    }

    @Test
    @DisplayName("Should generate JWT token successfully")
    void testGenerateJwtToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        
        String token = jwtUtils.generateJwtToken(authentication);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate client token successfully")
    void testGenerateClientToken() {
        String clientEmail = "client@example.com";
        
        String token = jwtUtils.generateClientToken(clientEmail);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals(clientEmail, username);
    }

    @Test
    @DisplayName("Should generate refresh token successfully")
    void testGenerateRefreshToken() {
        String username = "testuser";
        
        String token = jwtUtils.generateRefreshToken(username);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should extract username from JWT token")
    void testGetUserNameFromJwtToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtUtils.generateJwtToken(authentication);
        
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract roles from JWT token")
    void testGetRolesFromJwtToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER", "ROLE_ADMIN");
        String token = jwtUtils.generateJwtToken(authentication);
        
        String roles = jwtUtils.getRolesFromJwtToken(token);
        
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should validate valid JWT token")
    void testValidateJwtToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtUtils.generateJwtToken(authentication);
        
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid JWT token")
    void testValidateInvalidJwtToken() {
        String invalidToken = "invalid.jwt.token";
        
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed JWT token")
    void testValidateMalformedJwtToken() {
        String malformedToken = "malformed-token";
        
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty JWT token")
    void testValidateEmptyJwtToken() {
        boolean isValid = jwtUtils.validateJwtToken("");
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null JWT token")
    void testValidateNullJwtToken() {
        boolean isValid = jwtUtils.validateJwtToken(null);
        
        assertFalse(isValid);
    }

    private Authentication createMockAuthentication(String username, String... roles) {
        Collection<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .map(auth -> (GrantedAuthority) auth)
                .toList();
        
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(authorities)
                .build();
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        
        return authentication;
    }
}
