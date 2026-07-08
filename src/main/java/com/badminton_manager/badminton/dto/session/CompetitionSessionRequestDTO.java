package com.badminton_manager.badminton.dto.session;

import com.badminton_manager.badminton.enums.SessionStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class CompetitionSessionRequestDTO {
    private UUID organizerId;
    private String name;
    private SessionStatus status;
    private Integer pointsToWin;
    private Integer numberOfGames;
    private Boolean deuceRule;
    private Integer maxPoints;
    private Integer courtCount;
}
