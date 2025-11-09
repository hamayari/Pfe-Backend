package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/debug")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthDebugController {

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("authenticated", false);
            response.put("message", "No authentication found");
            return ResponseEntity.ok(response);
        }
        
        response.put("authenticated", authentication.isAuthenticated());
        response.put("username", authentication.getName());
        response.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}
