package com.badminton_manager.badminton.dto.auth;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponseDTO {
    private String token;
    private final String tokenType = "Bearer";
    private UUID id;
    private String name;
    private String email;

    public AuthResponseDTO(String token, UUID id, String name, String email) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
