package com.badminton_manager.badminton.model;

import com.badminton_manager.badminton.enums.RotationScreen;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "rotation_sessions")
public class RotationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false, unique = true)
    private User organizer;

    @Column(nullable = false)
    private int courts = 5;

    @Column(name = "max_rounds_per_player", nullable = false)
    private int maxRoundsPerPlayer = 6;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers = 36;

    @Column(name = "full_round_price", nullable = false, columnDefinition = "integer default 0")
    private int fullRoundPrice = 0;

    @Column(name = "court_names_csv")
    private String courtNamesCsv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RotationScreen screen = RotationScreen.setup;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
