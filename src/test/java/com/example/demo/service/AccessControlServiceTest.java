package com.example.demo.service;

import com.example.demo.enums.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccessControlService accessControlService;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        Role role = new Role();
        role.setName(ERole.ROLE_COMMERCIAL);

        testUser = new User();
        testUser.setId("USER-001");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Arrays.asList(role)));
    }

    @Test
    void testGetCurrentUser_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = accessControlService.getCurrentUser();

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        User result = accessControlService.getCurrentUser();

        assertNull(result);
    }

    @Test
    void testGetCurrentUser_NoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        User result = accessControlService.getCurrentUser();

        assertNull(result);
    }

    @Test
    void testGetCurrentUsername() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        String result = accessControlService.getCurrentUsername();

        assertEquals("testuser", result);
    }

    @Test
    void testHasRole_Commercial() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.hasRole("COMMERCIAL");

        assertTrue(result);
    }

    @Test
    void testHasRole_NotFound() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.hasRole("ADMIN");

        assertFalse(result);
    }

    @Test
    void testIsCommercial() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.isCommercial();

        assertTrue(result);
    }

    @Test
    void testIsProjectManager() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.isProjectManager();

        assertTrue(result);
    }

    @Test
    void testIsDecisionMaker() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_DECISION_MAKER")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.isDecisionMaker();

        assertTrue(result);
    }

    @Test
    void testIsAdmin() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.isAdmin();

        assertTrue(result);
    }

    @Test
    void testCanViewAllData_ProjectManager() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_PROJECT_MANAGER")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.canViewAllData();

        assertTrue(result);
    }

    @Test
    void testCanViewAllData_Commercial() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.canViewAllData();

        assertFalse(result);
    }

    @Test
    void testCanViewOnlyOwnData() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.canViewOnlyOwnData();

        assertTrue(result);
    }

    @Test
    void testCanAccessResource_OwnResource() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = accessControlService.canAccessResource("testuser");

        assertTrue(result);
    }

    @Test
    void testCanAccessResource_OtherResource_AsCommercial() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_COMMERCIAL")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = accessControlService.canAccessResource("otheruser");

        assertFalse(result);
    }

    @Test
    void testCanAccessResource_AsAdmin() {
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = accessControlService.canAccessResource("anyuser");

        assertTrue(result);
    }

    @Test
    void testLogCurrentUserInfo() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Should not throw exception
        assertDoesNotThrow(() -> accessControlService.logCurrentUserInfo());
    }

    @Test
    void testLogCurrentUserInfo_NoUser() {
        when(securityContext.getAuthentication()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> accessControlService.logCurrentUserInfo());
    }
}
