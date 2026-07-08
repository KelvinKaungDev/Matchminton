package com.badminton_manager.badminton.model;

import com.badminton_manager.badminton.enums.GameStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private CompetitionCourt court;

    @Column(name = "game_number", nullable = false)
    private int gameNumber;

    @Column(name = "team_a_score", nullable = false)
    private int teamAScore = 0;

    @Column(name = "team_b_score", nullable = false)
    private int teamBScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.active;

    @Enumerated(EnumType.STRING)
    @Column
    private CourtWinner winner;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
