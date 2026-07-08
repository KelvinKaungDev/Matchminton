package com.badminton_manager.badminton.service.auth;

import com.badminton_manager.badminton.dto.auth.AuthResponseDTO;
import com.badminton_manager.badminton.dto.auth.LoginRequestDTO;
import com.badminton_manager.badminton.dto.auth.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
}
