package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationConfigRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationScreenUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;

import java.util.UUID;

public interface RotationSessionService {
    RotationStateResponseDTO getStateByOrganizer(UUID organizerId);
    RotationStateResponseDTO updateConfig(UUID id, RotationConfigRequestDTO request);
    RotationStateResponseDTO markAllBench(UUID id);
    RotationStateResponseDTO start(UUID id);
    RotationStateResponseDTO fillEmptyCourts(UUID id);
    RotationStateResponseDTO updateScreen(UUID id, RotationScreenUpdateDTO request);
    RotationStateResponseDTO reset(UUID id);
}
