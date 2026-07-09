package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationCourtSwapRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;

import java.util.UUID;

public interface RotationCourtService {
    RotationStateResponseDTO complete(UUID id);
    RotationStateResponseDTO refill(UUID id);
    RotationStateResponseDTO swap(UUID id, RotationCourtSwapRequestDTO request);
}
