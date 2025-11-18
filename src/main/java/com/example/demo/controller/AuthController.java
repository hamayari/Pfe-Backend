package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.TwoFactorVerificationRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.payload.response.TwoFactorResponse;
import com.example.demo.payload.request.UserCreateRequest;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns JWT tokens")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token", description = "Refreshes the access token using a refresh token")
    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/authenticate-superadmin")
    @Operation(summary = "Authenticate super admin", description = "Authenticates a super admin with 2FA")
    public ResponseEntity<TwoFactorResponse> authenticateSuperAdmin(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.initiateSuperAdmin2FA(loginRequest));
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "Verify 2FA", description = "Verifies the 2FA code for super admin login")
    public ResponseEntity<JwtResponse> verifyTwoFactorCode(@Valid @RequestBody TwoFactorVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyTwoFactorCode(request));
    }

    @PostMapping("/signup")
    @Operation(summary = "Register user", description = "Registers a new user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.registerUser(signupRequest));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Initiates the password reset process")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            authService.initiatePasswordReset(email);
            return ResponseEntity.ok("Password reset email sent");
        } catch (Exception e) {
            System.err.println("❌ Erreur dans forgotPassword: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Completes the password reset process")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            authService.completePasswordReset(token, newPassword);
            return ResponseEntity.ok("Password has been reset successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user with the specified role")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal UserPrincipal creator) {
        User user = authService.createUserWithRole(request, creator.getUsername());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user account")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        authService.deleteUserAccount(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticateUser(loginRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Standard login endpoint that authenticates a user and returns JWT tokens")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticateUser(loginRequest);
    }
}
