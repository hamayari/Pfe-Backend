package com.example.demo.payload.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import com.example.demo.enums.ERole;

@Data
public class UserCreateRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    @NotBlank
    @Size(max = 50)
    private String firstName;
    
    @NotBlank
    @Size(max = 50)
    private String lastName;
    
    private String phoneNumber;
    private String structure;
    @SuppressWarnings("unused")
    private Set<String> roles;
    private Set<ERole> rolesEnum;
    private boolean temporaryPassword;
    private boolean forcePasswordChange;
    
    // Getter pour la compatibilité
    public Set<ERole> getRoles() {
        return rolesEnum;
    }
    
    public void setRoles(Set<ERole> roles) {
        this.rolesEnum = roles;
    }
    
    // Alias method for compatibility
    public String getNewPassword() {
        return password;
    }
}
