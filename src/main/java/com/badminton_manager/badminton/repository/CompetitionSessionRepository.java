package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.enums.SessionStatus;
import com.badminton_manager.badminton.model.CompetitionSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompetitionSessionRepository extends JpaRepository<CompetitionSession, UUID> {
    List<CompetitionSession> findByOrganizerId(UUID organizerId);
    List<CompetitionSession> findByStatus(SessionStatus status);
}
