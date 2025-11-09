package com.example.demo.security.jwt;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.demo.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Ignore JWT filter for public auth endpoints (but NOT 2FA endpoints which require authentication)
        AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/api/auth/signin");
        AntPathRequestMatcher signupMatcher = new AntPathRequestMatcher("/api/auth/signup");
        AntPathRequestMatcher refreshMatcher = new AntPathRequestMatcher("/api/auth/refresh");
        AntPathRequestMatcher forgotPasswordMatcher = new AntPathRequestMatcher("/api/auth/forgot-password");
        AntPathRequestMatcher resetPasswordMatcher = new AntPathRequestMatcher("/api/auth/reset-password");
        AntPathRequestMatcher wsMatcher = new AntPathRequestMatcher("/api/ws/**");
        AntPathRequestMatcher wsUltraSimpleMatcher = new AntPathRequestMatcher("/ws-ultra-simple");
        
        if (loginMatcher.matches(request) || signupMatcher.matches(request) || 
            refreshMatcher.matches(request) || forgotPasswordMatcher.matches(request) || 
            resetPasswordMatcher.matches(request) || wsMatcher.matches(request) || 
            wsUltraSimpleMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = parseJwt(request);
            if (jwt != null && !jwt.isBlank() && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Extraire les rôles du token JWT et les convertir en authorities
                String rolesFromToken = jwtUtils.getRolesFromJwtToken(jwt);
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                
                // Si le token contient des rôles, les utiliser au lieu de ceux de la base de données
                if (rolesFromToken != null && !rolesFromToken.isEmpty()) {
                    authorities = Arrays.stream(rolesFromToken.split(","))
                        .map(role -> (GrantedAuthority) () -> role.trim())
                        .collect(Collectors.toList());
                }
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set authentication to security context for user: {}", username);
                // Auto-presence: marquer l'utilisateur en 'online' à chaque requête authentifiée
                try {
                    userRepository.findByUsername(username).ifPresent(u -> {
                        u.setStatus("online");
                        u.setLastLoginAt(Instant.now());
                        userRepository.save(u);
                    });
                } catch (Exception ignored) {}
            } else if (jwt != null && !jwt.isBlank()) {
                logger.warn("Invalid JWT token found in request");
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        // Support token in query string for WebSocket/SockJS handshakes
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        return null;
    }
}
