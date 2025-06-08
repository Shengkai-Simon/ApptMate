package dev.skyang.authservice.model;

import lombok.Data;

@Data
public class RemoteUser {
    private Long id;
    private String username;
    private String email;
    private String password; // Hashed password
}
