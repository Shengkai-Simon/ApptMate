package dev.skyang.userservice.dto;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String password; // Added for internal auth endpoint

    // Default constructor
    public UserResponse() {
    }

    // Constructor for registration response (without password)
    public UserResponse(Long id, String username, String email, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor for internal user details response (with password)
    public UserResponse(Long id, String username, String email, Timestamp createdAt, Timestamp updatedAt, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.password = password;
    }

    // Setters are needed by @Data but explicitly defining them to show intent
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
