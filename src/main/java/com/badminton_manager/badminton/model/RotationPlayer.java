package com.badminton_manager.badminton.model;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.enums.Team;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "rotation_players")
public class RotationPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotation_session_id", nullable = false)
    private RotationSession rotationSession;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel skill = SkillLevel.B;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RotationPlayerStatus status = RotationPlayerStatus.waiting;

    @Column(name = "rounds_played", nullable = false)
    private int roundsPlayed = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id")
    private RotationCourt court;

    @Enumerated(EnumType.STRING)
    @Column
    private Team team;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
