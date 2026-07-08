package com.badminton_manager.badminton.dto.court;

import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import lombok.Data;

import java.util.UUID;

@Data
public class CompetitionCourtRequestDTO {
    private UUID sessionId;
    private int courtNumber;
    private String courtCode;
    private CourtStatus status;
    private CourtWinner winner;
}
