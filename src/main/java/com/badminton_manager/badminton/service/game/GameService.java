package com.badminton_manager.badminton.service.game;

import com.badminton_manager.badminton.dto.game.GameRequestDTO;
import com.badminton_manager.badminton.dto.game.GameResponseDTO;
import com.badminton_manager.badminton.enums.CourtWinner;

import java.util.List;
import java.util.UUID;

public interface GameService {
    List<GameResponseDTO> getByCourt(UUID courtId);
    GameResponseDTO getById(UUID id);
    GameResponseDTO create(GameRequestDTO request);
    GameResponseDTO update(UUID id, GameRequestDTO request);
    GameResponseDTO finishGame(UUID id, CourtWinner winner);
    void delete(UUID id);
}
