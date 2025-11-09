package com.example.demo.security;

import com.example.demo.security.jwt.AuthEntryPointJwt;
import com.example.demo.security.jwt.AuthTokenFilter;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsServiceImpl;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for the application.
 * Configures authentication, authorization, CORS, and security headers.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService, userRepository);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(new CustomAccessDeniedHandler()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/auth/signin",
                    "/api/auth/signup",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/init/**",         // Endpoints d'initialisation (bootstrap)
                    "/api/test/**", 
                    "/api/public/**",       // Endpoints publics pour n8n
                    "/api/conventions/**",  // Temporaire pour test
                    "/api/invoices/**",     // Temporaire pour test
                    "/api/conventions/test",
                    "/api/conventions/debug",
                    "/api/user-profile/*/phone", // Endpoint pour mise à jour numéro (sécurisé par validation)
                    "/api/decideur/health",     // Health check du chatbot
                    "/h2-console/**", 
                    "/swagger-ui/**", 
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/api/ws/**", 
                    "/ws/**", 
                    "/ws-messaging/**", 
                    "/ws-messaging-native", 
                    "/ws-simple", 
                    "/ws-ultra-simple",
                    "/actuator/health"
                ).permitAll()
                // 2FA endpoints require authentication
                .requestMatchers("/api/auth/2fa/**").authenticated()
                // Role-based access control
                .requestMatchers("/api/test/user").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/test/admin").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(
                    "/api/users/**",
                    "/api/roles/**"
                ).permitAll() // Temporairement désactivé pour le développement
                // Commercial role access
                .requestMatchers(
                    "/api/commercial/dashboard/**", 
                    "/api/conventions/**"
                ).hasAnyRole("COMMERCIAL", "SUPER_ADMIN")
                // Admin role access - Temporairement désactivé pour le développement
                .requestMatchers("/api/admin/**")
                    .permitAll() // .hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Nomenclatures - TEMPORAIREMENT OUVERT POUR DEBUG
                .requestMatchers(
                    "/api/applications/**",
                    "/api/zones-geographiques/**",
                    "/api/structures/**",
                    "/api/nomenclatures/**"
                ).permitAll() // TODO: Remettre hasAnyRole("ADMIN", "SUPER_ADMIN") après debug
                // Decision maker role access
                .requestMatchers(
                    "/api/decision-maker/dashboard/**",
                    "/api/decision-maker/reports/**",
                    "/api/decideur/**",
                    "/api/kpi-analysis/**",  // Endpoints KPI pour décideurs et chefs de projet
                    "/api/kpi/**",  // Endpoints KPI (analyse, alertes)
                    "/api/kpi-alerts/**",  // Endpoints gestion des alertes
                    "/api/conversations/**",  // Endpoints conversations/messaging
                    "/api/messages/**"  // Endpoints messages
                ).permitAll() // Temporairement pour le développement
                // Project manager role access
                .requestMatchers(
                    "/api/project-manager/dashboard/**",
                    "/api/project-manager/projects/**"
                ).hasRole("PROJECT_MANAGER")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000))
                .xssProtection(xss -> {}) // XSS protection is enabled by default in modern browsers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:;"))
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(contentType -> {})
            );

        // Add JWT filter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // Configure authentication provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
