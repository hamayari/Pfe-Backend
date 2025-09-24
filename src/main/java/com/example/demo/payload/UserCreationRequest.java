package com.example.demo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCreationRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String structure;
    private boolean temporaryPassword;
    private String role;
    private boolean forcePasswordChange;
    private String password;
}
