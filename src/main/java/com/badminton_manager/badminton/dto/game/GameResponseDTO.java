package com.badminton_manager.badminton.dto.game;

import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.enums.GameStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GameResponseDTO {
    private UUID id;
    private UUID courtId;
    private int courtNumber;
    private int gameNumber;
    private int teamAScore;
    private int teamBScore;
    private GameStatus status;
    private CourtWinner winner;
    private Instant startedAt;
    private Instant endedAt;
}
