package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.model.RotationSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RotationSessionRepository extends JpaRepository<RotationSession, UUID> {
    Optional<RotationSession> findByOrganizerId(UUID organizerId);
}
