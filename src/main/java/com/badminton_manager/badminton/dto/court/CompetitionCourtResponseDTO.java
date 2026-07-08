package com.badminton_manager.badminton.dto.court;

import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CompetitionCourtResponseDTO {
    private UUID id;
    private UUID sessionId;
    private String sessionName;
    private int courtNumber;
    private String courtCode;
    private CourtStatus status;
    private CourtWinner winner;
    private Instant createdAt;
}
