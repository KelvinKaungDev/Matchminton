package com.badminton_manager.badminton.dto.rotation;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.enums.Team;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class RotationPlayerResponseDTO {
    private UUID id;
    private UUID rotationSessionId;
    private String name;
    private SkillLevel skill;
    private RotationPlayerStatus status;
    private int roundsPlayed;
    private UUID courtId;
    private Team team;
    private Instant createdAt;
}
