package com.badminton_manager.badminton.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "rotation_round_history_entries")
public class RotationRoundHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rotation_session_id", nullable = false)
    private RotationSession rotationSession;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "court_number", nullable = false)
    private int courtNumber;

    @Column(name = "court_name", nullable = false)
    private String courtName;

    @Column(name = "team_a_names_csv", nullable = false)
    private String teamANamesCsv;

    @Column(name = "team_a_skills_csv", nullable = false)
    private String teamASkillsCsv;

    @Column(name = "team_b_names_csv", nullable = false)
    private String teamBNamesCsv;

    @Column(name = "team_b_skills_csv", nullable = false)
    private String teamBSkillsCsv;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
