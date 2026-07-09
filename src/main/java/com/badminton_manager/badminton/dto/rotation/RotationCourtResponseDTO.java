package com.badminton_manager.badminton.dto.rotation;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RotationCourtResponseDTO {
    private UUID id;
    private int courtNumber;
    private String name;
    private List<RotationPlayerResponseDTO> teamA;
    private List<RotationPlayerResponseDTO> teamB;
}
