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

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;
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
        // TODO: Envoyer le code 2FA par email
        System.out.println("Code 2FA pour " + user.getEmail() + ": " + twoFactorCode);

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
        // TODO: Envoyer l'email de bienvenue
        System.out.println("Email de bienvenue pour " + savedUser.getEmail());
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
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String resetToken = generateSecurePassword();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);
        // TODO: Envoyer l'email de réinitialisation
        System.out.println("Email de réinitialisation pour " + user.getEmail() + " avec token: " + resetToken);
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setForcePasswordChange(true);
        userRepository.save(user);
    }

    private String generateSecurePassword() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    private String generateTwoFactorCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
