package com.badminton_manager.badminton.dto.rotation;

import com.badminton_manager.badminton.enums.RotationScreen;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class RotationSessionResponseDTO {
    private UUID id;
    private UUID organizerId;
    private String organizerName;
    private int courts;
    private int maxRoundsPerPlayer;
    private int maxPlayers;
    private int fullRoundPrice;
    private List<String> courtNames;
    private RotationScreen screen;
    private Instant createdAt;
}
