package com.badminton_manager.badminton.dto.court;

import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class CompetitionCourtResultDTO {
    private UUID courtId;
    private String sessionName;
    private int courtNumber;
    private CourtStatus status;
    private CourtWinner winner;
    private Instant playedAt;
    // false when the organizer isn't Pro: names and scores are withheld
    private boolean shareEnabled;
    private List<String> teamAPlayers = List.of();
    private List<String> teamBPlayers = List.of();
    private List<GameScoreDTO> games = List.of();

    @Data
    public static class GameScoreDTO {
        private int gameNumber;
        private int teamAScore;
        private int teamBScore;
        private CourtWinner winner;
    }
}
