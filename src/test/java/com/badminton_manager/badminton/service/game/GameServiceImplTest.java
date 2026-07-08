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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CompetitionCourtRepository courtRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private CompetitionCourt court;
    private Game game;
    private UUID courtId;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        courtId = UUID.randomUUID();
        gameId = UUID.randomUUID();

        court = new CompetitionCourt();
        court.setId(courtId);
        court.setCourtNumber(1);

        game = new Game();
        game.setId(gameId);
        game.setCourt(court);
        game.setGameNumber(1);
        game.setTeamAScore(0);
        game.setTeamBScore(0);
        game.setStatus(GameStatus.active);
        game.setStartedAt(Instant.now());
    }

    @Test
    void getByCourt_returnsGamesForCourt() {
        when(gameRepository.findByCourtIdOrderByGameNumberAsc(courtId)).thenReturn(List.of(game));

        List<GameResponseDTO> result = gameService.getByCourt(courtId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGameNumber()).isEqualTo(1);
        assertThat(result.get(0).getCourtNumber()).isEqualTo(1);
    }

    @Test
    void getById_existingId_returnsGame() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        GameResponseDTO result = gameService.getById(gameId);

        assertThat(result.getId()).isEqualTo(gameId);
        assertThat(result.getStatus()).isEqualTo(GameStatus.active);
        assertThat(result.getTeamAScore()).isEqualTo(0);
        assertThat(result.getTeamBScore()).isEqualTo(0);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(gameRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void create_validRequest_savesAndReturnsGame() {
        GameRequestDTO request = new GameRequestDTO();
        request.setCourtId(courtId);
        request.setGameNumber(1);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        GameResponseDTO result = gameService.create(request);

        assertThat(result.getGameNumber()).isEqualTo(1);
        assertThat(result.getCourtNumber()).isEqualTo(1);
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void create_courtNotFound_throwsResourceNotFoundException() {
        UUID unknownCourtId = UUID.randomUUID();
        GameRequestDTO request = new GameRequestDTO();
        request.setCourtId(unknownCourtId);

        when(courtRepository.findById(unknownCourtId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(gameRepository, never()).save(any());
    }

    @Test
    void create_withScores_savesWithGivenScores() {
        GameRequestDTO request = new GameRequestDTO();
        request.setCourtId(courtId);
        request.setGameNumber(2);
        request.setTeamAScore(10);
        request.setTeamBScore(7);

        Game inProgressGame = new Game();
        inProgressGame.setId(UUID.randomUUID());
        inProgressGame.setCourt(court);
        inProgressGame.setGameNumber(2);
        inProgressGame.setTeamAScore(10);
        inProgressGame.setTeamBScore(7);
        inProgressGame.setStatus(GameStatus.active);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));
        when(gameRepository.save(any(Game.class))).thenReturn(inProgressGame);

        GameResponseDTO result = gameService.create(request);

        assertThat(result.getTeamAScore()).isEqualTo(10);
        assertThat(result.getTeamBScore()).isEqualTo(7);
    }

    @Test
    void update_existingId_updatesScores() {
        GameRequestDTO request = new GameRequestDTO();
        request.setTeamAScore(21);
        request.setTeamBScore(15);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponseDTO result = gameService.update(gameId, request);

        assertThat(result.getTeamAScore()).isEqualTo(21);
        assertThat(result.getTeamBScore()).isEqualTo(15);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(gameRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.update(unknownId, new GameRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void finishGame_setsStatusFinishedWinnerAndEndedAt() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        GameResponseDTO result = gameService.finishGame(gameId, CourtWinner.teamA);

        assertThat(result.getStatus()).isEqualTo(GameStatus.finished);
        assertThat(result.getWinner()).isEqualTo(CourtWinner.teamA);
        assertThat(result.getEndedAt()).isNotNull();
    }

    @Test
    void finishGame_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(gameRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.finishGame(unknownId, CourtWinner.teamB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesGame() {
        when(gameRepository.existsById(gameId)).thenReturn(true);

        gameService.delete(gameId);

        verify(gameRepository).deleteById(gameId);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(gameRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> gameService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(gameRepository, never()).deleteById(any());
    }
}
