package com.badminton_manager.badminton.model;

import com.badminton_manager.badminton.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "competition_sessions")
public class CompetitionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.active;

    @Column(name = "points_to_win", nullable = false)
    private int pointsToWin = 21;

    @Column(name = "number_of_games", nullable = false)
    private int numberOfGames = 3;

    @Column(name = "deuce_rule", nullable = false)
    private boolean deuceRule = true;

    @Column(name = "max_points", nullable = false)
    private int maxPoints = 30;

    @Column(name = "court_count", nullable = false)
    private int courtCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "ended_at")
    private Instant endedAt;
}
