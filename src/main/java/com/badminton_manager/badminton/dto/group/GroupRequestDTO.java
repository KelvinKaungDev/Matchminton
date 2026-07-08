package com.badminton_manager.badminton.dto.group;

import com.badminton_manager.badminton.enums.SkillLevel;
import lombok.Data;

import java.util.UUID;

@Data
public class GroupRequestDTO {
    private UUID organizerId;
    private String name;
    private SkillLevel skillLevel;
    private String photoUrl;
    private String lineId;
    private String description;
    private Boolean isActive;
}
