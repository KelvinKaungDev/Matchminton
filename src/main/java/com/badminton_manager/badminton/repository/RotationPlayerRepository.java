package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.model.RotationPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RotationPlayerRepository extends JpaRepository<RotationPlayer, UUID> {
    List<RotationPlayer> findByRotationSessionId(UUID rotationSessionId);
    List<RotationPlayer> findByRotationSessionIdAndStatus(UUID rotationSessionId, RotationPlayerStatus status);
    List<RotationPlayer> findByCourtId(UUID courtId);
    boolean existsByRotationSessionIdAndNameIgnoreCase(UUID rotationSessionId, String name);
}
