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
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "testSecretKeyForJWTTokenGenerationAndValidationTestWithMinimum256Bits";
    private static final int TEST_EXPIRATION = 3600000; // 1 hour
    private static final String TEST_ISSUER = "test-application";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtIssuer", TEST_ISSUER);
    }

    @Test
    @DisplayName("Should generate token successfully")
    void testGenerateToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("Should validate token and get username")
    void testValidateTokenAndGetUsername() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(authentication);
        
        String username = jwtTokenProvider.validateTokenAndGetUsername(token);
        
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(authentication);
        
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract username from token")
    void testGetUsernameFromToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(authentication);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract roles from token")
    void testGetRolesFromToken() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(authentication);
        
        String[] roles = jwtTokenProvider.getRolesFromToken(token);
        
        assertNotNull(roles);
        assertTrue(roles.length >= 1);
        assertTrue(Arrays.asList(roles).contains("ROLE_USER") || Arrays.asList(roles).contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void testExtractExpiration() {
        Authentication authentication = createMockAuthentication("testuser", "ROLE_USER");
        String token = jwtTokenProvider.generateToken(authentication);
        
        Date expiration = jwtTokenProvider.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should return null for invalid token signature")
    void testValidateTokenWithInvalidSignature() {
        String tokenWithInvalidSignature = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0.invalid";
        
        String username = jwtTokenProvider.validateTokenAndGetUsername(tokenWithInvalidSignature);
        
        assertNull(username);
    }

    @Test
    @DisplayName("Should return null for malformed token")
    void testValidateTokenMalformed() {
        String malformedToken = "malformed-token";
        
        String username = jwtTokenProvider.validateTokenAndGetUsername(malformedToken);
        
        assertNull(username);
    }

    @Test
    @DisplayName("Should return null for empty token")
    void testValidateEmptyToken() {
        String username = jwtTokenProvider.validateTokenAndGetUsername("");
        
        assertNull(username);
    }

    @Test
    @DisplayName("Should handle token with multiple roles")
    void testTokenWithMultipleRoles() {
        Authentication authentication = createMockAuthentication("admin", "ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");
        String token = jwtTokenProvider.generateToken(authentication);
        
        String username = jwtTokenProvider.getUsernameFromToken(token);
        String[] roles = jwtTokenProvider.getRolesFromToken(token);
        
        assertEquals("admin", username);
        assertTrue(roles.length >= 1);
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
