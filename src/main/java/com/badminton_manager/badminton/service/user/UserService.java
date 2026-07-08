package com.badminton_manager.badminton.service.user;

import com.badminton_manager.badminton.dto.user.UserRequestDTO;
import com.badminton_manager.badminton.dto.user.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponseDTO> getAll();
    UserResponseDTO getById(UUID id);
    UserResponseDTO create(UserRequestDTO request);
    UserResponseDTO update(UUID id, UserRequestDTO request);
    void delete(UUID id);
}
