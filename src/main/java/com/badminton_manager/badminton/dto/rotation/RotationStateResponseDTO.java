package com.badminton_manager.badminton.dto.rotation;

import lombok.Data;

import java.util.List;

@Data
public class RotationStateResponseDTO {
    private RotationSessionResponseDTO session;
    private List<RotationPlayerResponseDTO> players;
    private List<RotationCourtResponseDTO> courts;
    private List<RotationHistoryEntryResponseDTO> history;
}
