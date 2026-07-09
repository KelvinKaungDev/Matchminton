package com.badminton_manager.badminton.dto.rotation;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.SkillLevel;
import lombok.Data;

import java.util.UUID;

@Data
public class RotationPlayerRequestDTO {
    private UUID rotationSessionId;
    private String name;
    private SkillLevel skill;
    private RotationPlayerStatus status;
}
