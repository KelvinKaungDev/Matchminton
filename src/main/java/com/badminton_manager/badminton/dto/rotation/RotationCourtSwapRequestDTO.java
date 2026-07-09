package com.badminton_manager.badminton.dto.rotation;

import lombok.Data;

import java.util.UUID;

@Data
public class RotationCourtSwapRequestDTO {
    private UUID courtPlayerId;
    private UUID benchPlayerId;
}
