package com.example.demo.service;

import com.example.demo.enums.ActionType;
import com.example.demo.enums.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.TwoFactorVerificationRequest;
import com.example.demo.payload.request.UserCreateRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.payload.response.TwoFactorResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        // Re-fetch user to ensure ID is loaded
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found after authentication"));

        // Vérification blocage utilisateur
        if (!user.isActive()) {
            String reason = user.getBlockReason() != null ? user.getBlockReason() : "Votre compte a été bloqué par un administrateur.";
            throw new UnauthorizedException("Votre compte est bloqué : " + reason);
        }

        // Vérification du rôle demandé (DÉSACTIVÉE pour permettre la connexion)
        // if (loginRequest.getRole() != null && !loginRequest.getRole().isEmpty()) {
        //     boolean isSuperAdmin = userDetails.getAuthorities().stream()
        //         .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"));

        //     if (!isSuperAdmin) { // Si ce n'est pas un superadmin, on vérifie le rôle
        //         boolean hasRole = userDetails.getAuthorities().stream()
        //             .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase(loginRequest.getRole()));
        //         if (!hasRole) {
        //             // Log l'erreur mais ne pas bloquer la connexion
        //             System.out.println("Warning: User " + userDetails.getUsername() + " tried to access role " + loginRequest.getRole() + " but doesn't have it.");
        //             // throw new UnauthorizedException("Vous n'avez pas le rôle requis pour cette connexion.");
        //         }
        //     }
        // }

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toList());

        return new JwtResponse(
            jwt,
            user.getId(), // Use ID from the re-fetched user object
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles,
            user.isForcePasswordChange()
        );
    }

    @Transactional
    public JwtResponse authenticateSuperAdmin(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.hasRole(ERole.ROLE_SUPER_ADMIN)) {
            throw new UnauthorizedException("User is not a super admin");
        }

        return authenticateUser(loginRequest);
    }

    @Transactional
    public JwtResponse verifyTwoFactorCode(TwoFactorVerificationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found"));
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUsername());
        loginRequest.setPassword("");
        return authenticateUser(loginRequest);
    }

    @Transactional
    public JwtResponse refreshToken(String refreshToken, String ipAddress) {
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, user.getAuthorities());
        String newAccessToken = jwtUtils.generateJwtToken(authentication);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        List<String> roles = user.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toList());

        return new JwtResponse(
            newAccessToken,
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            roles,
            user.isForcePasswordChange()
        );
    }

    @Transactional
    public TwoFactorResponse initiateSuperAdmin2FA(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isSuperAdmin()) {
            throw new UnauthorizedException("User is not a super admin");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String twoFactorCode = generateTwoFactorCode();
        
        // Envoyer le code 2FA par email
        try {
            emailService.send2FACode(user.getEmail(), twoFactorCode, user.getUsername());
            logger.info("Code 2FA envoyé à l'email: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du code 2FA: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible d'envoyer le code 2FA");
        }

        return new TwoFactorResponse("Two-factor authentication code sent", true, null);
    }

    @Transactional
    public User registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = new User(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> userRoles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
            userRoles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role userRole = roleRepository.findByName(ERole.valueOf(role))
                    .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
                userRoles.add(userRole);
            });
        }

        user.setRoles(userRoles);
        User savedUser = userRepository.save(user);
        auditLogService.logUserAction(savedUser, ActionType.USER_CREATED, "User registered successfully");
        
        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
            logger.info("Email de bienvenue envoyé à: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.warn("Impossible d'envoyer l'email de bienvenue: {}", e.getMessage());
        }
        
        return savedUser;
    }

    @Transactional
    public User createUserWithRole(UserCreateRequest request, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new UnauthorizedException("Creator not found"));

        if (!creator.isAdmin() && !creator.isSuperAdmin()) {
            throw new UnauthorizedException("Only admins can create users");
        }

        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()));

        Set<ERole> roles = request.getRoles();
        Set<Role> userRoles = new HashSet<>();

        if (roles == null || roles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
            userRoles.add(userRole);
        } else {
            roles.forEach(role -> {
                Role userRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new BadRequestException("Error: Role is not found."));
                userRoles.add(userRole);
            });
        }

        user.setRoles(userRoles);
        User savedUser = userRepository.save(user);
        auditLogService.logUserAction(savedUser, ActionType.USER_CREATED, "User created by " + creatorUsername);
        // TODO: Envoyer l'email de bienvenue
        System.out.println("Email de bienvenue pour " + savedUser.getEmail());
        return savedUser;
    }

    @Transactional
    public void deleteUserAccount(String userId) {
        User user = userRepository.findById(userId.toString()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        auditLogService.logUserAction(user, ActionType.USER_DELETED, "User deleted");
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        // 🔒 SÉCURITÉ: Ne pas révéler si l'email existe ou non
        // Toujours retourner un succès, mais n'envoyer l'email que si l'utilisateur existe
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            // L'email n'existe pas dans la base de données
            logger.warn("⚠️ Tentative de réinitialisation pour un email inexistant: {}", email);
            // Ne pas lancer d'exception pour ne pas révéler que l'email n'existe pas
            // L'utilisateur verra le même message de succès
            return;
        }
        
        User user = userOpt.get();
        
        // Vérifier que l'utilisateur est actif
        if (!user.isActive()) {
            logger.warn("⚠️ Tentative de réinitialisation pour un compte bloqué: {}", email);
            // Ne pas envoyer d'email pour un compte bloqué
            return;
        }
        
        String resetToken = generateSecurePassword();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        
        logger.info("🔐 Token généré pour {}: {}", user.getEmail(), resetToken);
        logger.info("🔐 Expiration du token: {}", user.getResetTokenExpiry());
        
        User savedUser = userRepository.save(user);
        logger.info("✅ Token sauvegardé dans la base de données pour: {}", savedUser.getEmail());
        
        logger.info("🔐 Demande de réinitialisation de mot de passe pour: {}", user.getEmail());
        
        // Envoyer l'email de réinitialisation
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            logger.info("✅ Email de réinitialisation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage());
            // Ne pas bloquer le processus même si l'email échoue
        }
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        logger.info("🔍 Recherche du token: {}", token);
        
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            logger.error("❌ Token non trouvé dans la base de données: {}", token);
            throw new BadRequestException("Token invalide ou expiré");
        }
        
        User user = userOpt.get();
        logger.info("✅ Token trouvé pour l'utilisateur: {}", user.getEmail());
        
        // Vérifier si le token n'est pas expiré
        if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry().isBefore(Instant.now())) {
            logger.error("❌ Token expiré pour: {}. Expiration: {}, Maintenant: {}", 
                user.getEmail(), user.getResetTokenExpiry(), Instant.now());
            throw new BadRequestException("Le token de réinitialisation a expiré. Veuillez faire une nouvelle demande.");
        }
        
        logger.info("✅ Token valide pour: {}", user.getEmail());
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setForcePasswordChange(false); // Pas besoin de forcer le changement après reset
        userRepository.save(user);
        
        logger.info("✅ Mot de passe réinitialisé avec succès pour: {}", user.getEmail());
        auditLogService.logUserAction(user, ActionType.UPDATE, "Mot de passe réinitialisé via email");
    }

    private String generateSecurePassword() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        // Utiliser l'encodeur URL-safe pour éviter les problèmes avec les caractères spéciaux dans l'URL
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        System.out.println("========================================");
        System.out.println("🔑 TOKEN GÉNÉRÉ: " + token);
        System.out.println("========================================");
        return token;
    }

    private String generateTwoFactorCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
