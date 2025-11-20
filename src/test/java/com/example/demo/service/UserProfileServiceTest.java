package com.example.demo.service;

import com.example.demo.model.UserProfile;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private UserProfileService service;

    private UserProfile mockProfile;
    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        mockProfile = createMockProfile();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentUserProfile_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.of(mockProfile));

        // When
        UserProfile result = service.getCurrentUserProfile();

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(userProfileRepository, times(1)).findByEmail(testEmail);
    }

    @Test
    void testGetCurrentUserProfile_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When/Then
        assertThrows(AccessDeniedException.class, () -> {
            service.getCurrentUserProfile();
        });
    }

    @Test
    void testGetCurrentUserProfile_AuthenticationNotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When/Then
        assertThrows(AccessDeniedException.class, () -> {
            service.getCurrentUserProfile();
        });
    }

    @Test
    void testGetCurrentUserProfile_ProfileNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(AccessDeniedException.class, () -> {
            service.getCurrentUserProfile();
        });
    }

    @Test
    void testUpdateProfile_Success() {
        // Given
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setFirstName("Jane");
        updatedProfile.setLastName("Smith");
        updatedProfile.setPhoneNumber("+21698765432");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.of(mockProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfile result = service.updateProfile(updatedProfile);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("+21698765432", result.getPhoneNumber());
        assertNotNull(result.getLastUpdated());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testUpdateProfile_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);
        UserProfile updatedProfile = new UserProfile();

        // When/Then
        assertThrows(AccessDeniedException.class, () -> {
            service.updateProfile(updatedProfile);
        });
    }

    @Test
    void testUpdateProfile_ProfileNotFound() {
        // Given
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setFirstName("Jane");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(AccessDeniedException.class, () -> {
            service.updateProfile(updatedProfile);
        });
    }

    @Test
    void testUpdateProfile_OnlyAllowedFieldsUpdated() {
        // Given
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setFirstName("Jane");
        updatedProfile.setLastName("Smith");
        updatedProfile.setPhoneNumber("+21698765432");
        updatedProfile.setEmail("newemail@example.com"); // Should not be updated

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.of(mockProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfile result = service.updateProfile(updatedProfile);

        // Then
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("+21698765432", result.getPhoneNumber());
        // Email should remain unchanged
        assertEquals(testEmail, result.getEmail());
    }

    @Test
    void testUpdateProfile_LastUpdatedIsSet() {
        // Given
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setFirstName("Jane");
        LocalDateTime beforeUpdate = LocalDateTime.now();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getEmail()).thenReturn(testEmail);
        when(userProfileRepository.findByEmail(testEmail))
            .thenReturn(Optional.of(mockProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfile result = service.updateProfile(updatedProfile);

        // Then
        assertNotNull(result.getLastUpdated());
        assertTrue(result.getLastUpdated().isAfter(beforeUpdate) || 
                   result.getLastUpdated().isEqual(beforeUpdate));
    }

    // Helper method
    private UserProfile createMockProfile() {
        UserProfile profile = new UserProfile();
        profile.setId("profile123");
        profile.setEmail(testEmail);
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setPhoneNumber("+21612345678");
        profile.setLastUpdated(LocalDateTime.now().minusDays(1));
        return profile;
    }
}
