package com.badminton_manager.badminton.service.game;

import com.badminton_manager.badminton.dto.game.GameRequestDTO;
import com.badminton_manager.badminton.dto.game.GameResponseDTO;
import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.enums.GameStatus;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionCourt;
import com.badminton_manager.badminton.model.Game;
import com.badminton_manager.badminton.repository.CompetitionCourtRepository;
import com.badminton_manager.badminton.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final CompetitionCourtRepository courtRepository;

    public GameServiceImpl(GameRepository gameRepository, CompetitionCourtRepository courtRepository) {
        this.gameRepository = gameRepository;
        this.courtRepository = courtRepository;
    }

    @Override
    public List<GameResponseDTO> getByCourt(UUID courtId) {
        return gameRepository.findByCourtIdOrderByGameNumberAsc(courtId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GameResponseDTO getById(UUID id) {
        return gameRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));
    }

    @Override
    public GameResponseDTO create(GameRequestDTO request) {
        CompetitionCourt court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));

        Game game = new Game();
        game.setCourt(court);
        game.setGameNumber(request.getGameNumber());
        if (request.getTeamAScore() != null) game.setTeamAScore(request.getTeamAScore());
        if (request.getTeamBScore() != null) game.setTeamBScore(request.getTeamBScore());
        if (request.getStatus() != null) game.setStatus(request.getStatus());
        game.setWinner(request.getWinner());
        game.setStartedAt(Instant.now());

        return toResponse(gameRepository.save(game));
    }

    @Override
    public GameResponseDTO update(UUID id, GameRequestDTO request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));

        if (request.getCourtId() != null) {
            CompetitionCourt court = courtRepository.findById(request.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));
            game.setCourt(court);
        }
        if (request.getTeamAScore() != null) game.setTeamAScore(request.getTeamAScore());
        if (request.getTeamBScore() != null) game.setTeamBScore(request.getTeamBScore());
        if (request.getStatus() != null) game.setStatus(request.getStatus());
        game.setWinner(request.getWinner());

        return toResponse(gameRepository.save(game));
    }

    @Override
    public GameResponseDTO finishGame(UUID id, CourtWinner winner) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));
        game.setStatus(GameStatus.finished);
        game.setWinner(winner);
        game.setEndedAt(Instant.now());
        return toResponse(gameRepository.save(game));
    }

    @Override
    public void delete(UUID id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Game not found with id: " + id);
        }
        gameRepository.deleteById(id);
    }

    private GameResponseDTO toResponse(Game game) {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setId(game.getId());
        dto.setCourtId(game.getCourt().getId());
        dto.setCourtNumber(game.getCourt().getCourtNumber());
        dto.setGameNumber(game.getGameNumber());
        dto.setTeamAScore(game.getTeamAScore());
        dto.setTeamBScore(game.getTeamBScore());
        dto.setStatus(game.getStatus());
        dto.setWinner(game.getWinner());
        dto.setStartedAt(game.getStartedAt());
        dto.setEndedAt(game.getEndedAt());
        return dto;
    }
}
