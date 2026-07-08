package com.badminton_manager.badminton.service.player;

import com.badminton_manager.badminton.dto.player.PlayerRequestDTO;
import com.badminton_manager.badminton.dto.player.PlayerResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PlayerService {
    List<PlayerResponseDTO> getByCourt(UUID courtId);
    PlayerResponseDTO getById(UUID id);
    PlayerResponseDTO create(PlayerRequestDTO request);
    PlayerResponseDTO update(UUID id, PlayerRequestDTO request);
    void delete(UUID id);
}
