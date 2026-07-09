package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.model.RotationRoundHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RotationRoundHistoryEntryRepository extends JpaRepository<RotationRoundHistoryEntry, UUID> {
    List<RotationRoundHistoryEntry> findByRotationSessionIdOrderByRoundNumberAsc(UUID rotationSessionId);
    void deleteByRotationSessionId(UUID rotationSessionId);
}
