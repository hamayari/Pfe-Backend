package com.example.demo.service;

import com.example.demo.enums.ActionType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthService
 * Couverture: 80%+
 * Bonnes pratiques: AAA (Arrange-Act-Assert), Mockito, AssertJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests Unitaires")
class AuthServiceTest {

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

    private User testUser;
    private Role userRole;
    private Role adminRole;
    private Role superAdminRole;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Rôles
        userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        superAdminRole = new Role();
        superAdminRole.setName(ERole.ROLE_SUPER_ADMIN);

        // Arrange - Utilisateur de test
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));

        // Arrange - LoginRequest
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    // ==================== AUTHENTICATE USER ====================

    @Test
    @DisplayName("authenticateUser - Devrait authentifier l'utilisateur avec succès")
    void authenticateUser_ShouldAuthenticateSuccessfully() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            testUser,
            false
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("refresh-token");

        // Act
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("authenticateUser - Devrait échouer si utilisateur non trouvé après authentification")
    void authenticateUser_ShouldFail_WhenUserNotFoundAfterAuth() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(
            "user123",
            "testuser",
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            testUser,
            false
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("User not found after authentication");
    }

    @Test
    @DisplayName("authenticateUser - Devrait échouer si utilisateur bloqué")
    void authenticateUser_ShouldFail_WhenUserBlocked() {
        // Arrange
        testUser.setIsActive(false);
        testUser.setBlockReason("Compte suspendu");

        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            testUser,
            false
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticateUser(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Votre compte est bloqué");
    }

    // ==================== AUTHENTICATE SUPER ADMIN ====================

    @Test
    @DisplayName("authenticateSuperAdmin - Devrait authentifier le super admin")
    void authenticateSuperAdmin_ShouldAuthenticateSuccessfully() {
        // Arrange
        testUser.setRoles(Set.of(superAdminRole));

        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")),
            testUser,
            false
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("refresh-token");

        // Act
        JwtResponse response = authService.authenticateSuperAdmin(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    @Test
    @DisplayName("authenticateSuperAdmin - Devrait échouer si utilisateur n'est pas super admin")
    void authenticateSuperAdmin_ShouldFail_WhenNotSuperAdmin() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticateSuperAdmin(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("not a super admin");
    }

    @Test
    @DisplayName("authenticateSuperAdmin - Devrait échouer si utilisateur non trouvé")
    void authenticateSuperAdmin_ShouldFail_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticateSuperAdmin(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("User not found");
    }

    // ==================== REFRESH TOKEN ====================

    @Test
    @DisplayName("refreshToken - Devrait rafraîchir le token avec succès")
    void refreshToken_ShouldRefreshSuccessfully() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String ipAddress = "192.168.1.1";

        when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateJwtToken(any(Authentication.class))).thenReturn("new-jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        // Act
        JwtResponse response = authService.refreshToken(refreshToken, ipAddress);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(jwtUtils).validateJwtToken(refreshToken);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("refreshToken - Devrait échouer si token invalide")
    void refreshToken_ShouldFail_WhenTokenInvalid() {
        // Arrange
        String refreshToken = "invalid-token";
        String ipAddress = "192.168.1.1";

        when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken, ipAddress))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("refreshToken - Devrait échouer si utilisateur non trouvé")
    void refreshToken_ShouldFail_WhenUserNotFound() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String ipAddress = "192.168.1.1";

        when(jwtUtils.validateJwtToken(refreshToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken, ipAddress))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("User not found");
    }

    // ==================== TWO FACTOR AUTHENTICATION ====================

    @Test
    @DisplayName("initiateSuperAdmin2FA - Devrait initier 2FA pour super admin")
    void initiateSuperAdmin2FA_ShouldInitiateSuccessfully() {
        // Arrange
        testUser.setRoles(Set.of(superAdminRole));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        doNothing().when(emailService).send2FACode(anyString(), anyString(), anyString());

        // Act
        TwoFactorResponse response = authService.initiateSuperAdmin2FA(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isRequiresTwoFactor()).isTrue();
        verify(emailService).send2FACode(eq(testUser.getEmail()), anyString(), eq(testUser.getUsername()));
    }

    @Test
    @DisplayName("initiateSuperAdmin2FA - Devrait échouer si utilisateur n'est pas super admin")
    void initiateSuperAdmin2FA_ShouldFail_WhenNotSuperAdmin() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.initiateSuperAdmin2FA(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("not a super admin");
    }

    @Test
    @DisplayName("initiateSuperAdmin2FA - Devrait échouer si mot de passe invalide")
    void initiateSuperAdmin2FA_ShouldFail_WhenInvalidPassword() {
        // Arrange
        testUser.setRoles(Set.of(superAdminRole));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.initiateSuperAdmin2FA(loginRequest))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Invalid password");
    }

    @Test
    @DisplayName("initiateSuperAdmin2FA - Devrait échouer si erreur d'envoi email")
    void initiateSuperAdmin2FA_ShouldFail_WhenEmailSendFails() {
        // Arrange
        testUser.setRoles(Set.of(superAdminRole));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        doThrow(new RuntimeException("Email service error"))
            .when(emailService).send2FACode(anyString(), anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> authService.initiateSuperAdmin2FA(loginRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Impossible d'envoyer le code 2FA");
    }

    @Test
    @DisplayName("verifyTwoFactorCode - Devrait vérifier le code 2FA")
    void verifyTwoFactorCode_ShouldVerifySuccessfully() {
        // Arrange
        TwoFactorVerificationRequest request = new TwoFactorVerificationRequest();
        request.setUsername("testuser");
        request.setCode("123456");

        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(
            testUser.getId(),
            testUser.getUsername(),
            testUser.getEmail(),
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            testUser,
            false
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("refresh-token");

        // Act
        JwtResponse response = authService.verifyTwoFactorCode(request);

        // Assert
        assertThat(response).isNotNull();
        verify(userRepository, times(2)).findByUsername("testuser");
    }

    // ==================== REGISTER USER ====================

    @Test
    @DisplayName("registerUser - Devrait enregistrer un nouvel utilisateur")
    void registerUser_ShouldRegisterSuccessfully() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(Set.of("ROLE_USER"));

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(auditLogService).logUserAction(any(User.class), eq(ActionType.USER_CREATED), anyString());
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Act
        User result = authService.registerUser(signupRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail("newuser@example.com", "newuser");
    }

    @Test
    @DisplayName("registerUser - Devrait échouer si username déjà pris")
    void registerUser_ShouldFail_WhenUsernameExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(signupRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Username is already taken");
    }

    @Test
    @DisplayName("registerUser - Devrait échouer si email déjà utilisé")
    void registerUser_ShouldFail_WhenEmailExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(signupRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Email is already in use");
    }

    @Test
    @DisplayName("registerUser - Devrait assigner ROLE_USER par défaut si aucun rôle spécifié")
    void registerUser_ShouldAssignDefaultRole_WhenNoRolesSpecified() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRoles(null); // Aucun rôle spécifié

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(auditLogService).logUserAction(any(User.class), eq(ActionType.USER_CREATED), anyString());

        // Act
        User result = authService.registerUser(signupRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(roleRepository).findByName(ERole.ROLE_USER);
    }

    // ==================== CREATE USER WITH ROLE ====================

    @Test
    @DisplayName("createUserWithRole - Admin devrait créer un utilisateur")
    void createUserWithRole_ShouldCreateUser_WhenCreatorIsAdmin() {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRoles(Set.of(adminRole));

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setRoles(Set.of(ERole.ROLE_USER));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(auditLogService).logUserAction(any(User.class), eq(ActionType.USER_CREATED), anyString());

        // Act
        User result = authService.createUserWithRole(request, "admin");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUserWithRole - Devrait échouer si créateur n'est pas admin")
    void createUserWithRole_ShouldFail_WhenCreatorNotAdmin() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.createUserWithRole(request, "testuser"))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Only admins can create users");
    }

    @Test
    @DisplayName("createUserWithRole - Devrait échouer si créateur non trouvé")
    void createUserWithRole_ShouldFail_WhenCreatorNotFound() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest();

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.createUserWithRole(request, "unknown"))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Creator not found");
    }

    // ==================== DELETE USER ACCOUNT ====================

    @Test
    @DisplayName("deleteUserAccount - Devrait supprimer le compte utilisateur")
    void deleteUserAccount_ShouldDeleteSuccessfully() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);
        doNothing().when(auditLogService).logUserAction(any(User.class), eq(ActionType.USER_DELETED), anyString());

        // Act
        authService.deleteUserAccount("user123");

        // Assert
        verify(userRepository).delete(testUser);
        verify(auditLogService).logUserAction(testUser, ActionType.USER_DELETED, "User deleted");
    }

    @Test
    @DisplayName("deleteUserAccount - Devrait échouer si utilisateur non trouvé")
    void deleteUserAccount_ShouldFail_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.deleteUserAccount("invalid"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    // ==================== PASSWORD RESET ====================

    @Test
    @DisplayName("initiatePasswordReset - Devrait initier la réinitialisation du mot de passe")
    void initiatePasswordReset_ShouldInitiateSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // Act
        authService.initiatePasswordReset("test@example.com");

        // Assert
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("initiatePasswordReset - Ne devrait pas échouer si email inexistant (sécurité)")
    void initiatePasswordReset_ShouldNotFail_WhenEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        authService.initiatePasswordReset("nonexistent@example.com");

        // Assert
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("initiatePasswordReset - Ne devrait pas envoyer email si compte bloqué")
    void initiatePasswordReset_ShouldNotSendEmail_WhenAccountBlocked() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        authService.initiatePasswordReset("test@example.com");

        // Assert
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("completePasswordReset - Devrait compléter la réinitialisation du mot de passe")
    void completePasswordReset_ShouldCompleteSuccessfully() {
        // Arrange
        String resetToken = "valid-token";
        testUser.setResetToken(resetToken);
        testUser.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));

        when(userRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(auditLogService).logUserAction(any(User.class), eq(ActionType.UPDATE), anyString());

        // Act
        authService.completePasswordReset(resetToken, "newPassword123");

        // Assert
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logUserAction(any(User.class), eq(ActionType.UPDATE), anyString());
    }

    @Test
    @DisplayName("completePasswordReset - Devrait échouer si token invalide")
    void completePasswordReset_ShouldFail_WhenTokenInvalid() {
        // Arrange
        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.completePasswordReset("invalid-token", "newPassword"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Token invalide ou expiré");
    }

    @Test
    @DisplayName("completePasswordReset - Devrait échouer si token expiré")
    void completePasswordReset_ShouldFail_WhenTokenExpired() {
        // Arrange
        String resetToken = "expired-token";
        testUser.setResetToken(resetToken);
        testUser.setResetTokenExpiry(Instant.now().minus(1, ChronoUnit.HOURS)); // Expiré

        when(userRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.completePasswordReset(resetToken, "newPassword"))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("token de réinitialisation a expiré");
    }
}
