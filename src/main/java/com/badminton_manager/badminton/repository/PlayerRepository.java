package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.enums.Team;
import com.badminton_manager.badminton.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    List<Player> findByCourtId(UUID courtId);
    List<Player> findByCourtIdAndTeam(UUID courtId, Team team);
}
