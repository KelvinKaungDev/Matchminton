package com.badminton_manager.badminton.dto.rotation;

import lombok.Data;

import java.util.List;

@Data
public class RotationConfigRequestDTO {
    private Integer courts;
    private Integer maxRoundsPerPlayer;
    private Integer maxPlayers;
    private Integer fullRoundPrice;
    private List<String> courtNames;
}
