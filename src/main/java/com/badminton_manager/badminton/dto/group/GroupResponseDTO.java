package com.badminton_manager.badminton.dto.group;

import com.badminton_manager.badminton.enums.SkillLevel;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GroupResponseDTO {
    private UUID id;
    private UUID organizerId;
    private String organizerName;
    private String name;
    private SkillLevel skillLevel;
    private String photoUrl;
    private String lineId;
    private String description;
    private boolean isActive;
    private Instant createdAt;
}
