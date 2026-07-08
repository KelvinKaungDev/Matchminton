package com.badminton_manager.badminton.dto.player;

import com.badminton_manager.badminton.enums.Team;
import lombok.Data;

import java.util.UUID;

@Data
public class PlayerRequestDTO {
    private UUID courtId;
    private String name;
    private Team team;
}
