package com.example.demo.service;

import com.example.demo.model.RoleEnum;
import com.example.demo.model.User;
import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.response.JwtResponse;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {
    
    @Autowired
    private AuthService authService;

    @Test
    void registerUser_Success() {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setRoles(Set.of("ROLE_COMMERCIAL"));
        
        User user = authService.registerUser(request);
        assertNotNull(user);
    }

    @Test
    void authenticateUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");
        
        JwtResponse response = authService.authenticateUser(request);
        assertNotNull(response);
    }
}
