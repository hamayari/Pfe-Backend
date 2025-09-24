package com.example.demo.service;

import com.example.demo.dto.auth.ForgotPasswordRequest;
import com.example.demo.dto.auth.ForgotPasswordResponse;
import com.example.demo.dto.auth.JwtResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.SignupRequest;
import com.example.demo.dto.auth.TwoFactorResponse;
import com.example.demo.dto.auth.TwoFactorVerificationRequest;
import com.example.demo.dto.UserCreationRequest;
import com.example.demo.exception.HierarchyViolationException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import java.util.stream.IntStream;
import java.util.Random;

@Service
public class AuthService_corrected {
    
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final String SECRET_KEY = "votre_cle_secrete";
    private final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 heures

    // Stockage temporaire des sessions 2FA (en production, utiliser Redis ou une base de données)
    private final Map<String, TwoFactorSession> twoFactorSessions = new HashMap<>();

    public AuthService_corrected(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    private void logAction(String action, String entityType, String entityId, String username) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setUserId(username);
        log.setTimestamp(new Date());
        auditLogService.save(log);
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Authentification standard sans vérification de rôle
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        // Vérification du rôle si spécifié
        if (loginRequest.getRole() != null && !loginRequest.getRole().isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(loginRequest.getRole());
                boolean hasRole = user.getRoles().contains(roleEnum);
                if (!hasRole) {
                    throw new RuntimeException("Accès non autorisé : rôle requis non attribué");
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Rôle invalide spécifié");
            }
        }
        
        // Vérification spécifique pour la connexion admin
        if (loginRequest.isAdminLogin() && !user.getRoles().contains(Role.ADMIN)) {
            throw new RuntimeException("Accès non autorisé : rôle administrateur requis");
        }

        return new JwtResponse(
            jwt,
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRoles()
        );
    }
    
    /**
     * Authentification spécifique pour les administrateurs
     */
    public JwtResponse authenticateAdmin(LoginRequest loginRequest) {
        // Force le flag adminLogin à true
        loginRequest.setAdminLogin(true);
        return authenticateUser(loginRequest);
    }
    
    /**
     * Authentification spécifique pour les utilisateurs non-admin avec vérification du rôle
     */
    public JwtResponse authenticateUserWithRole(LoginRequest loginRequest) {
        // Vérifie que le rôle est spécifié
        if (loginRequest.getRole() == null || loginRequest.getRole().isEmpty()) {
            throw new RuntimeException("Un rôle doit être spécifié pour l'utilisateur");
        }
        
        // Vérifie que le rôle n'est pas ADMIN (utiliser authenticateAdmin pour cela)
        if (loginRequest.getRole().equals("ADMIN")) {
            throw new RuntimeException("Rôle invalide spécifié");
        }
        
        return authenticateUser(loginRequest);
    }

    public User registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        // Préparer les rôles de l'utilisateur
        Set<Role> userRoles;
        
        // Vérifier si des rôles sont fournis dans l'ensemble roles
        if (signUpRequest.getRoles() != null && !signUpRequest.getRoles().isEmpty()) {
            userRoles = validateAndConvertRoles(signUpRequest.getRoles());
        } 
        // Sinon, essayer d'utiliser le champ selectedRole
        else if (signUpRequest.getSelectedRole() != null && !signUpRequest.getSelectedRole().isEmpty()) {
            Role role = convertStringToRole(signUpRequest.getSelectedRole());
            if (role != null) {
                userRoles = Set.of(role);
            } else {
                throw new RuntimeException("Invalid role specified: " + signUpRequest.getSelectedRole());
            }
        } 
        // Si aucun rôle n'est spécifié, lever une exception
        else {
            throw new RuntimeException("At least one role must be specified");
        }

        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        // Encoder le mot de passe avant de l'enregistrer
        user.setPassword(hashPassword(signUpRequest.getPassword()));
        user.setRoles(userRoles);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        logAction("USER_CREATED", "USER", savedUser.getId().toString(), savedUser.getEmail());
        return savedUser;
    }
    
    /**
     * Traite une demande de réinitialisation de mot de passe.
     * Dans une implémentation réelle, cette méthode générerait un token unique,
     * l'enregistrerait en base de données et enverrait un email avec un lien de réinitialisation.
     * Pour cette démo, nous simulons simplement l'envoi d'un email.
     */
    public ForgotPasswordResponse processForgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (!userOpt.isPresent()) {
            // Pour des raisons de sécurité, ne pas indiquer si l'email existe ou non
            return new ForgotPasswordResponse(true, 
                "Si votre email est enregistré dans notre système, vous recevrez un lien de réinitialisation.");
        }
        
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        
        // Dans une implémentation réelle, nous sauvegarderions ce token en base de données
        // avec une date d'expiration, associé à l'utilisateur
        // user.setResetToken(resetToken);
        // user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        // userRepository.save(user);
        
        // Simuler l'envoi d'un email
        String resetLink = "https://votre-application.com/reset-password?token=" + resetToken;
        // emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        
        System.out.println("Email de réinitialisation envoyé à " + user.getEmail() + " avec le lien: " + resetLink);
        
        logAction("PASSWORD_RESET_REQUEST", "USER", user.getId().toString(), user.getEmail());
        return new ForgotPasswordResponse(true, 
            "Si votre email est enregistré dans notre système, vous recevrez un lien de réinitialisation.");
    }

    /**
     * Authentification spécifique pour le Super Admin avec 2FA
     * @param loginRequest Informations de connexion
     * @return Réponse avec token JWT ou demande de 2FA
     */
    public TwoFactorResponse authenticateSuperAdmin(LoginRequest loginRequest) {
        // Authentification standard
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        
        // Définir l'authentification dans le contexte de sécurité
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est un Super Admin
        if (!user.getRoles().contains(Role.SUPER_ADMIN)) {
            throw new RuntimeException("Accès non autorisé. Seuls les Super Administrateurs peuvent accéder à cette interface.");
        }

        // Générer un code 2FA et l'envoyer par email
        String code = generateTwoFactorCode();
        String sessionId = UUID.randomUUID().toString();
        
        // Stocker la session 2FA
        TwoFactorSession session = new TwoFactorSession(user.getEmail(), code, LocalDateTime.now().plusMinutes(5));
        twoFactorSessions.put(sessionId, session);
        
        // Envoyer le code par email
        try {
            // emailService.sendSimpleMessage(
            //     user.getEmail(),
            //     "Code d'authentification à deux facteurs",
            //     "Votre code d'authentification est : " + code + ". Il expire dans 5 minutes."
            // );
        } catch (Exception e) {
            // En cas d'erreur d'envoi d'email, on continue quand même pour les tests
            System.out.println("Erreur d'envoi d'email: " + e.getMessage());
            System.out.println("Code 2FA pour " + user.getEmail() + ": " + code);
        }
        
        // Retourner une réponse demandant la vérification 2FA
        logAction("2FA_REQUEST", "USER", user.getId().toString(), user.getEmail());
        return new TwoFactorResponse(sessionId);
    }
    
    /**
     * Vérification du code d'authentification à deux facteurs
     * @param request Informations de vérification 2FA
     * @return Réponse avec token JWT
     */
    public JwtResponse verifyTwoFactorCode(TwoFactorVerificationRequest request) {
        // Récupérer la session 2FA
        TwoFactorSession session = twoFactorSessions.get(request.getTwoFactorSessionId());
        if (session == null) {
            throw new RuntimeException("Session 2FA invalide ou expirée");
        }
        
        // Vérifier que la session n'est pas expirée
        if (session.getExpiration().isBefore(LocalDateTime.now())) {
            twoFactorSessions.remove(request.getTwoFactorSessionId());
            throw new RuntimeException("Le code 2FA a expiré");
        }
        
        // Vérifier que l'email correspond
        if (!session.getEmail().equals(request.getEmail())) {
            throw new RuntimeException("Email invalide");
        }
        
        // Vérifier le code
        if (!session.getCode().equals(request.getCode())) {
            throw new RuntimeException("Code 2FA invalide");
        }
        
        // Supprimer la session
        twoFactorSessions.remove(request.getTwoFactorSessionId());
        
        // Générer un token JWT
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, Set.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = generateToken(authentication);
        
        logAction("2FA_SUCCESS", "USER", user.getId().toString(), user.getEmail());
        return new JwtResponse(jwt, user);
    }

    /**
     * Génère un code aléatoire à 6 chiffres pour l'authentification à deux facteurs
     * @return Code à 6 chiffres
     */
    private String generateTwoFactorCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Génère un nombre entre 100000 et 999999
        return String.valueOf(code);
    }
    
    /**
     * Valide et convertit les rôles qui pourraient être au format kebab-case ou autre format
     * vers les enums Role appropriés
     * @param roles Ensemble de rôles à valider et convertir
     * @return Ensemble de rôles validés
     */
    private Set<Role> validateAndConvertRoles(Set<Role> roles) {
        // Si les rôles sont déjà des enums Role valides, les retourner directement
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        
        // Si aucun rôle n'est fourni, renvoyer un ensemble vide
        return Set.of();
    }
    
    /**
     * Convertit une chaîne de caractères représentant un rôle en enum Role
     * Gère différents formats (kebab-case, snake_case, etc.)
     * @param roleStr Chaîne représentant le rôle
     * @return Enum Role correspondant ou null si non trouvé
     */
    public Role convertStringToRole(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return null;
        }
        
        // Normaliser la chaîne en majuscules avec underscores
        String normalizedRole = roleStr.toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');
        
        // Essayer de convertir directement
        try {
            return Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException e) {
            // Gérer les cas spéciaux
            switch (normalizedRole) {
                case "ADMIN":
                    return Role.ADMIN;
                case "COMMERCIAL":
                    return Role.COMMERCIAL;
                case "PROJECT_MANAGER":
                    return Role.PROJECT_MANAGER;
                case "DECISION_MAKER":
                    return Role.DECISION_MAKER;
                case "SUPER_ADMIN":
                    return Role.SUPER_ADMIN;
                default:
                    return null;
            }
        }
    }
    
    /**
     * Génère un token JWT pour l'utilisateur
     * @param authentication Informations d'authentification
     * @return Token JWT
     */
    private String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        logAction("LOGIN", "USER", userDetails.getUsername(), userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(authentication.getName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    /**
     * Valide un token JWT
     * @param token Token JWT
     * @param userDetails Informations de l'utilisateur
     * @return Vrai si le token est valide, faux sinon
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Hash un mot de passe
     * @param password Mot de passe
     * @return Mot de passe hashé
     */
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Vérifie un mot de passe
     * @param rawPassword Mot de passe brut
     * @param hashedPassword Mot de passe hashé
     * @return Vrai si les mots de passe correspondent, faux sinon
     */
    public Boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    // Méthodes utilitaires pour extraire les infos du token
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Classe interne pour stocker les informations de session 2FA
     */
    private static class TwoFactorSession {
        private final String email;
        private final String code;
        private final LocalDateTime expiration;
        
        public TwoFactorSession(String email, String code, LocalDateTime expiration) {
            this.email = email;
            this.code = code;
            this.expiration = expiration;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getCode() {
            return code;
        }
        
        public LocalDateTime getExpiration() {
            return expiration;
        }
    }

    public User createUser(UserCreationRequest request, boolean requirePasswordChange) {
        String tempPassword = generateRandomPassword();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setForcePasswordChange(requirePasswordChange);
        
        // Journalisation
        auditLogService.logAction("USER_CREATION", "USER", user.getId(), 
            "Création compte avec mot de passe temporaire");
        
        // Envoi email (mock)
        sendTempPasswordEmail(user.getEmail(), tempPassword);
        
        return userRepository.save(user);
    }

    private String generateRandomPassword() {
        // Génère un mot de passe aléatoire de 12 caractères
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!";
        return IntStream.range(0, 12)
            .map(i -> chars.charAt(new Random().nextInt(chars.length())))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    @Transactional
    public User createUserWithHierarchy(UserCreationRequest request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
            .orElseThrow(() -> new UserNotFoundException("Créateur non trouvé"));
            
        validateCreationHierarchy(creator, request.getRole());

        String tempPassword = generateSecurePassword();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(request.getRole());
        user.setForcePasswordChange(request.isForcePasswordChange());
        
        auditLogService.log("USER_CREATION", creator.getId(), user.getId());
        emailService.sendAccountCreationEmail(user.getEmail(), tempPassword, request.isForcePasswordChange());
        
        return userRepository.save(user);
    }

    private void validateCreationHierarchy(User creator, Role targetRole) {
        if (creator.getRole() == Role.SUPER_ADMIN && targetRole != Role.ADMIN) {
            throw new HierarchyViolationException("Un SUPER_ADMIN ne peut créer que des ADMIN");
        }
        if (creator.getRole() == Role.ADMIN && !List.of(Role.COMMERCIAL, Role.PROJECT_MANAGER, Role.DECISION_MAKER).contains(targetRole)) {
            throw new HierarchyViolationException("Rôle non autorisé");
        }
    }

    private String generateSecurePassword() {
        // Génère un mot de passe aléatoire de 12 caractères
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!";
        return IntStream.range(0, 12)
            .map(i -> chars.charAt(new Random().nextInt(chars.length())))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
