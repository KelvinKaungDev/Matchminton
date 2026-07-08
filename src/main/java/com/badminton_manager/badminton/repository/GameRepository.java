package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findByCourtId(UUID courtId);
    List<Game> findByCourtIdOrderByGameNumberAsc(UUID courtId);
}
