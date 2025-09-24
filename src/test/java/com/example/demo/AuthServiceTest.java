package com.example.demo;

import com.example.demo.payload.SignupRequest;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegisterAndLogin() {
        // 1. Create a signup request
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");

        try {
            // 2. Register a new user
            System.out.println("Attempting to register a new user...");
            User registeredUser = authService.registerUser(signUpRequest);
            System.out.println("Registration successful: " + registeredUser);
            assertNotNull(registeredUser);
            assertNotNull(registeredUser.getId());
            
            // 3. Create a login request
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("password");
            
            // 4. Login with the registered user
            System.out.println("Attempting to login...");
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            System.out.println("Login successful: " + jwtResponse);
            assertNotNull(jwtResponse);
            assertNotNull(jwtResponse.getToken());
            System.out.println("JWT Token: " + jwtResponse.getToken());
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
