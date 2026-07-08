package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.model.CompetitionCourt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetitionCourtRepository extends JpaRepository<CompetitionCourt, UUID> {
    List<CompetitionCourt> findBySessionId(UUID sessionId);
    List<CompetitionCourt> findBySessionIdAndStatus(UUID sessionId, CourtStatus status);
    Optional<CompetitionCourt> findByCourtCode(String courtCode);
}
