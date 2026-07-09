package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.model.RotationCourt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RotationCourtRepository extends JpaRepository<RotationCourt, UUID> {
    List<RotationCourt> findByRotationSessionId(UUID rotationSessionId);
    List<RotationCourt> findByRotationSessionIdOrderByCourtNumberAsc(UUID rotationSessionId);
}
