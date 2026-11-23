/*
 * Developer: Shreyansh
 * Development Date: 17-11-2026
 */
package com.unikart.user;

import java.time.LocalDateTime;

public class User {

    // User roles supported by Unikart
    public enum Role {
        CUSTOMER,
        SELLER,
        ADMIN
    }

    // Fields matching the users database table
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;

    // Empty constructor
    public User() {
    }

    // Constructor for creating a user object
    public User(String name, String email,
                String passwordHash, String phone, Role role) {

        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
    }

    // Getters and setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Display user information without exposing the password hash
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
