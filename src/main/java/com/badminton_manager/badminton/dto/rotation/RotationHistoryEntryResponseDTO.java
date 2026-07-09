package com.badminton_manager.badminton.dto.rotation;

import lombok.Data;

import java.util.List;

@Data
public class RotationHistoryEntryResponseDTO {
    private int roundNumber;
    private int courtNumber;
    private String courtName;
    private List<RotationHistoryPlayerDTO> teamA;
    private List<RotationHistoryPlayerDTO> teamB;
}
