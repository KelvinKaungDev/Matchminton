package com.badminton_manager.badminton.dto.session;

import com.badminton_manager.badminton.enums.SessionStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CompetitionSessionResponseDTO {
    private UUID id;
    private UUID organizerId;
    private String organizerName;
    private String name;
    private SessionStatus status;
    private int pointsToWin;
    private int numberOfGames;
    private boolean deuceRule;
    private int maxPoints;
    private int courtCount;
    private Instant createdAt;
    private Instant endedAt;
}
