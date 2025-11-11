package com.example.demo.service;

import com.example.demo.enums.ERole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.TwoFactorVerificationRequest;
import com.example.demo.payload.request.UserCreateRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.payload.response.TwoFactorResponse;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires détaillés pour AuthService
 * Utilise Mockito pour simuler les dépendances
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceDetailedTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Role mockRole;
    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private Authentication mockAuthentication;
    private UserPrincipal mockUserPrincipal;

    @BeforeEach
    void setUp() {
        // Initialisation des données de test
        mockRole = new Role();
        mockRole.setName(ERole.ROLE_ADMIN);

        mockUser = new User();
        mockUser.setId("123");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");
        // mockUser.setActive(true); // Adapter selon votre classe User (peut-être setIsActive ou autre)
        mockUser.setRoles(Set.of(mockRole));

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_USER"));

        // Mock UserPrincipal
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        // Adapter le constructeur UserPrincipal selon votre implémentation
        mockUserPrincipal = mock(UserPrincipal.class);
        lenient().when(mockUserPrincipal.getUsername()).thenReturn("testuser");
        lenient().when(mockUserPrincipal.getEmail()).thenReturn("test@example.com");
        lenient().when(mockUserPrincipal.getAuthorities()).thenReturn((Collection) authorities);

        // Mock Authentication
        mockAuthentication = mock(Authentication.class);
        lenient().when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrincipal);
    }

    // ==================== Tests d'authentification ====================

    @Test
    @DisplayName("Authentification réussie avec credentials valides")
    void testAuthenticateUser_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateJwtToken(any(Authentication.class)))
            .thenReturn("mock-jwt-token");
        when(jwtUtils.generateRefreshToken(anyString()))
            .thenReturn("mock-refresh-token");

        // When
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtUtils, times(1)).generateJwtToken(any());
    }

    @Test
    @DisplayName("Authentification échoue avec credentials invalides")
    void testAuthenticateUser_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    @DisplayName("Authentification échoue si utilisateur n'existe pas après authentification")
    void testAuthenticateUser_UserNotFoundAfterAuth() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticateUser(loginRequest);
        });

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    // Test commenté - Adapter selon votre implémentation de User
    // @Test
    // @DisplayName("Authentification échoue si compte utilisateur est bloqué")
    // void testAuthenticateUser_UserBlocked() {
    //     // Given
    //     mockUser.setIsActive(false); // ou mockUser.setActive(false)
    //     mockUser.setBlockReason("Compte suspendu pour violation des règles");
    //
    //     when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
    //         .thenReturn(mockAuthentication);
    //     when(userRepository.findByUsername("testuser"))
    //         .thenReturn(Optional.of(mockUser));
    //
    //     // When & Then
    //     UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
    //         authService.authenticateUser(loginRequest);
    //     });
    //
    //     assertTrue(exception.getMessage().contains("bloqué"));
    //     verify(jwtUtils, never()).generateJwtToken(any());
    // }

    @Test
    @DisplayName("Authentification génère token de rafraîchissement")
    void testAuthenticateUser_GeneratesRefreshToken() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mockAuthentication);
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateJwtToken(any(Authentication.class)))
            .thenReturn("access-token");
        when(jwtUtils.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Then
        assertNotNull(response);
        verify(jwtUtils, times(1)).generateRefreshToken("testuser");
    }

    // ==================== Tests d'authentification Super Admin ====================

    @Test
    @DisplayName("Authentification Super Admin réussie")
    void testAuthenticateSuperAdmin_Success() {
        // Given
        mockUser.setRoles(Set.of(createRole(ERole.ROLE_SUPER_ADMIN)));
        
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any()))
            .thenReturn(mockAuthentication);
        when(jwtUtils.generateJwtToken(any()))
            .thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken(anyString()))
            .thenReturn("refresh-token");

        // When
        JwtResponse response = authService.authenticateSuperAdmin(loginRequest);

        // Then
        assertNotNull(response);
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Authentification Super Admin échoue si pas super admin")
    void testAuthenticateSuperAdmin_NotSuperAdmin() {
        // Given
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(mockUser));

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticateSuperAdmin(loginRequest);
        });
    }

    // ==================== Tests d'inscription ====================

    @Test
    @DisplayName("Inscription d'un nouvel utilisateur réussie")
    void testRegisterUser_Success() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER))
            .thenReturn(Optional.of(createRole(ERole.ROLE_USER)));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        User result = authService.registerUser(signupRequest);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Inscription échoue si username déjà utilisé")
    void testRegisterUser_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            authService.registerUser(signupRequest);
        });

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inscription échoue si email déjà utilisé")
    void testRegisterUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            authService.registerUser(signupRequest);
        });

        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inscription encode le mot de passe")
    void testRegisterUser_EncodesPassword() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("super-secure-encoded-password");
        when(roleRepository.findByName(any())).thenReturn(Optional.of(createRole(ERole.ROLE_USER)));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        authService.registerUser(signupRequest);

        // Then
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // ==================== Tests de création d'utilisateur ====================

    @Test
    @DisplayName("Création d'utilisateur avec rôle par admin")
    void testCreateUserWithRole_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        // request.setRole("ROLE_COMMERCIAL"); // Adapter selon UserCreateRequest (peut-être setRoles)

        lenient().when(userRepository.existsByUsername("newuser")).thenReturn(false);
        lenient().when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER))
            .thenReturn(Optional.of(createRole(ERole.ROLE_USER)));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        // Mock du créateur (admin) avec rôle ADMIN
        User adminUser = new User();
        adminUser.setId("admin-id");
        adminUser.setUsername("admin");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(createRole(ERole.ROLE_ADMIN));
        adminUser.setRoles(adminRoles);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        User result = authService.createUserWithRole(request, "admin");

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        // verify(auditLogService, times(1)).logAction(...); // Adapter selon la signature de votre méthode
    }

    // ==================== Tests de réinitialisation de mot de passe ====================

    @Test
    @DisplayName("Initiation de réinitialisation de mot de passe")
    void testInitiatePasswordReset_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // When
        authService.initiatePasswordReset(email);

        // Then
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("Initiation de réinitialisation ne révèle pas si email n'existe pas (sécurité)")
    void testInitiatePasswordReset_EmailNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then - Ne doit PAS lancer d'exception pour des raisons de sécurité
        assertDoesNotThrow(() -> {
            authService.initiatePasswordReset(email);
        });
        
        // Vérifier qu'aucun email n'a été envoyé
        verify(userRepository, times(1)).findByEmail(email);
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // Test commenté - Nécessite l'implémentation de password reset dans User
    // @Test
    // @DisplayName("Complétion de réinitialisation de mot de passe")
    // void testCompletePasswordReset_Success() {
    //     // Given
    //     String token = "valid-reset-token";
    //     String newPassword = "newPassword123";
    //     
    //     mockUser.setPasswordResetToken(token);
    //     mockUser.setPasswordResetTokenExpiry(new Date(System.currentTimeMillis() + 3600000));
    //
    //     when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(mockUser));
    //     when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
    //     when(userRepository.save(any(User.class))).thenReturn(mockUser);
    //
    //     // When
    //     authService.completePasswordReset(token, newPassword);
    //
    //     // Then
    //     verify(userRepository, times(1)).findByPasswordResetToken(token);
    //     verify(passwordEncoder, times(1)).encode(newPassword);
    //     verify(userRepository, times(1)).save(any(User.class));
    // }

    // Test commenté - Nécessite l'implémentation de password reset
    // @Test
    // @DisplayName("Complétion de réinitialisation échoue si token invalide")
    // void testCompletePasswordReset_InvalidToken() {
    //     // Given
    //     String token = "invalid-token";
    //     when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.empty());
    //
    //     // When & Then
    //     assertThrows(BadRequestException.class, () -> {
    //         authService.completePasswordReset(token, "newPassword");
    //     });
    //
    //     verify(passwordEncoder, never()).encode(anyString());
    // }

    // Test commenté - Nécessite l'implémentation de password reset
    // @Test
    // @DisplayName("Complétion de réinitialisation échoue si token expiré")
    // void testCompletePasswordReset_ExpiredToken() {
    //     // Given
    //     String token = "expired-token";
    //     mockUser.setPasswordResetToken(token);
    //     mockUser.setPasswordResetTokenExpiry(new Date(System.currentTimeMillis() - 3600000));
    //
    //     when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(mockUser));
    //
    //     // When & Then
    //     assertThrows(BadRequestException.class, () -> {
    //         authService.completePasswordReset(token, "newPassword");
    //     });
    //
    //     verify(passwordEncoder, never()).encode(anyString());
    // }

    // ==================== Tests de suppression d'utilisateur ====================

    @Test
    @DisplayName("Suppression d'utilisateur réussie")
    void testDeleteUserAccount_Success() {
        // Given
        String userId = "123";
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).delete(any(User.class));

        // When
        authService.deleteUserAccount(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(mockUser);
    }

    @Test
    @DisplayName("Suppression échoue si utilisateur n'existe pas")
    void testDeleteUserAccount_UserNotFound() {
        // Given
        String userId = "nonexistent";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            authService.deleteUserAccount(userId);
        });

        verify(userRepository, never()).delete(any());
    }

    // ==================== Tests de refresh token ====================

    @Test
    @DisplayName("Refresh token réussie")
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        String ipAddress = "192.168.1.1";

        when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        // When
        JwtResponse response = authService.refreshToken(refreshToken, ipAddress);

        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        verify(jwtUtils, times(1)).validateJwtToken(refreshToken);
        verify(jwtUtils, times(1)).generateJwtToken(any());
    }

    @Test
    @DisplayName("Refresh token échoue si token invalide")
    void testRefreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            authService.refreshToken(invalidToken, "192.168.1.1");
        });

        verify(jwtUtils, never()).generateJwtToken(any());
    }

    // ==================== Tests de vérification 2FA ====================

    @Test
    @DisplayName("Vérification 2FA réussie")
    void testVerifyTwoFactorCode_Success() {
        // Given
        TwoFactorVerificationRequest request = new TwoFactorVerificationRequest();
        request.setUsername("testuser");
        request.setCode("123456");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refresh-token");

        // When
        JwtResponse response = authService.verifyTwoFactorCode(request);

        // Then
        assertNotNull(response);
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    // ==================== Méthodes utilitaires ====================

    private Role createRole(ERole roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
