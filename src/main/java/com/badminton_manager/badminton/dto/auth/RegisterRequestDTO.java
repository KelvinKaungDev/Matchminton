package com.badminton_manager.badminton.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String name;
    private String email;
    private String password;
}
