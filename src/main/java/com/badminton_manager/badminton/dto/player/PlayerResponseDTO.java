package com.badminton_manager.badminton.dto.player;

import com.badminton_manager.badminton.enums.Team;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PlayerResponseDTO {
    private UUID id;
    private UUID courtId;
    private int courtNumber;
    private String name;
    private Team team;
    private Instant createdAt;
}
