package com.badminton_manager.badminton.dto.rotation;

import com.badminton_manager.badminton.enums.SkillLevel;
import lombok.Data;

@Data
public class RotationHistoryPlayerDTO {
    private String name;
    private SkillLevel skill;
}
