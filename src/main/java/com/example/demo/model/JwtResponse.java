package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class JwtResponse {
    private final String token;
    private final String id;
    private final String username;
    private final String email;
    private final List<String> roles;
    private final boolean mustChangePassword;

    public JwtResponse(String token, String id, String username, 
                      String email, List<String> roles, boolean mustChangePassword) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = new ArrayList<>(roles);
        this.mustChangePassword = mustChangePassword;
    }

    // Getters
    public String getToken() { return token; }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
    public boolean mustChangePassword() { return mustChangePassword; }

    // toString
    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='" + token + '\'' +
                ", id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", mustChangePassword=" + mustChangePassword +
                '}';
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JwtResponse that = (JwtResponse) o;

        if (mustChangePassword != that.mustChangePassword) return false;
        if (!token.equals(that.token)) return false;
        if (!id.equals(that.id)) return false;
        if (!username.equals(that.username)) return false;
        if (!email.equals(that.email)) return false;
        return roles.equals(that.roles);
    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + roles.hashCode();
        result = 31 * result + (mustChangePassword ? 1 : 0);
        return result;
    }
}
