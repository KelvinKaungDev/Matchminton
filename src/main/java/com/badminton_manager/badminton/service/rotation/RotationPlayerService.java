package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationPlayerRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerStatusUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;

import java.util.UUID;

public interface RotationPlayerService {
    RotationStateResponseDTO create(RotationPlayerRequestDTO request);
    RotationStateResponseDTO update(UUID id, RotationPlayerUpdateDTO request);
    RotationStateResponseDTO updateStatus(UUID id, RotationPlayerStatusUpdateDTO request);
    RotationStateResponseDTO leave(UUID id);
    void delete(UUID id);
}
