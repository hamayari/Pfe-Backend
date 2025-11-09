package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final UserRepository userRepository;

    /**
     * Génère un secret et un QR Code pour activer 2FA
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generate2FASecret(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info("🔐 Génération du secret 2FA pour l'utilisateur: {}", username);

            // Générer un nouveau secret
            String secret = twoFactorAuthService.generateSecret();
            log.info("✅ Secret généré: {}", secret.substring(0, 4) + "...");

            // Générer le QR Code
            String qrCodeUrl = twoFactorAuthService.generateQrCodeUrl(username, secret);
            log.info("✅ QR Code généré pour: {}", username);

            // Sauvegarder temporairement le secret (sera confirmé lors de l'activation)
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setTwoFactorSecret(secret);
            user.setTwoFactorEnabled(false); // Pas encore activé
            userRepository.save(user);
            log.info("✅ Secret sauvegardé temporairement pour: {}", username);

            Map<String, String> response = new HashMap<>();
            response.put("secret", secret);
            response.put("qrCodeUrl", qrCodeUrl);
            response.put("googleAuthUrl", twoFactorAuthService.generateGoogleAuthenticatorUrl(username, secret));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erreur génération QR Code: {}", e.getMessage(), e);
            log.error("❌ Erreur inattendue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la génération du secret 2FA"));
        }
    }

    /**
     * Active l'authentification 2FA après vérification du code
     */
    @PostMapping("/enable")
    public ResponseEntity<?> enable2FA(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String code = request.get("code");
            String secret = request.get("secret");

            log.info("🔐 Activation 2FA pour l'utilisateur: {}", username);
            log.info("📝 Code reçu: {}", code);

            if (code == null || code.length() != 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le code doit contenir 6 chiffres"));
            }

            // Récupérer l'utilisateur
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Utiliser le secret fourni ou celui sauvegardé
            String secretToVerify = secret != null ? secret : user.getTwoFactorSecret();

            if (secretToVerify == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Aucun secret 2FA trouvé. Veuillez générer un nouveau QR Code."));
            }

            // Vérifier le code
            boolean isValid = twoFactorAuthService.verifyCode(secretToVerify, code);
            log.info("🔍 Vérification du code: {}", isValid ? "✅ Valide" : "❌ Invalide");

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Code incorrect"));
            }

            // Activer 2FA
            user.setTwoFactorEnabled(true);
            user.setTwoFactorSecret(secretToVerify);
            userRepository.save(user);
            log.info("✅ 2FA activé pour l'utilisateur: {}", username);

            return ResponseEntity.ok(Map.of(
                    "message", "Authentification à deux facteurs activée avec succès",
                    "enabled", true
            ));

        } catch (Exception e) {
            log.error("❌ Erreur activation 2FA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'activation de 2FA"));
        }
    }

    /**
     * Désactive l'authentification 2FA
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String code = request.get("code");

            log.info("🔐 Désactivation 2FA pour l'utilisateur: {}", username);

            if (code == null || code.length() != 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le code doit contenir 6 chiffres"));
            }

            // Récupérer l'utilisateur
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!user.isTwoFactorEnabled()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'authentification 2FA n'est pas activée"));
            }

            // Vérifier le code avant de désactiver
            boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
            log.info("🔍 Vérification du code: {}", isValid ? "✅ Valide" : "❌ Invalide");

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Code incorrect"));
            }

            // Désactiver 2FA
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            userRepository.save(user);
            log.info("✅ 2FA désactivé pour l'utilisateur: {}", username);

            return ResponseEntity.ok(Map.of(
                    "message", "Authentification à deux facteurs désactivée",
                    "enabled", false
            ));

        } catch (Exception e) {
            log.error("❌ Erreur désactivation 2FA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la désactivation de 2FA"));
        }
    }

    /**
     * Vérifie le statut 2FA de l'utilisateur
     */
    @GetMapping("/status")
    public ResponseEntity<?> get2FAStatus(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Gérer le cas où le champ n'existe pas encore (anciens utilisateurs)
            boolean enabled = user.isTwoFactorEnabled();

            return ResponseEntity.ok(Map.of(
                    "enabled", enabled,
                    "username", username
            ));

        } catch (Exception e) {
            log.error("❌ Erreur récupération statut 2FA: {}", e.getMessage(), e);
            // Retourner false par défaut en cas d'erreur
            return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "username", authentication.getName()
            ));
        }
    }

    /**
     * Vérifie un code 2FA lors de la connexion
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify2FACode(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String code = request.get("code");

            log.info("🔐 Vérification code 2FA pour: {}", username);

            if (code == null || code.length() != 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le code doit contenir 6 chiffres"));
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!user.isTwoFactorEnabled()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'authentification 2FA n'est pas activée"));
            }

            boolean isValid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
            log.info("🔍 Code 2FA: {}", isValid ? "✅ Valide" : "❌ Invalide");

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Code incorrect", "valid", false));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Code vérifié avec succès",
                    "valid", true
            ));

        } catch (Exception e) {
            log.error("❌ Erreur vérification code 2FA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la vérification du code"));
        }
    }
}
