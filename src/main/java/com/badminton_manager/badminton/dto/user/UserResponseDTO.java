package com.badminton_manager.badminton.dto.user;

import com.badminton_manager.badminton.enums.Provider;
import com.badminton_manager.badminton.enums.Tier;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String avatar;
    private Provider provider;
    private Tier tier;
    private Instant createdAt;
}
