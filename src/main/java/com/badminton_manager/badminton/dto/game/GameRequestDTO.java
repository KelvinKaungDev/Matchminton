package com.badminton_manager.badminton.dto.game;

import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.enums.GameStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class GameRequestDTO {
    private UUID courtId;
    private int gameNumber;
    private Integer teamAScore;
    private Integer teamBScore;
    private GameStatus status;
    private CourtWinner winner;
}
