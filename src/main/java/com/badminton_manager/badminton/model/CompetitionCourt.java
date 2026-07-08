package com.badminton_manager.badminton.model;

import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "competition_courts")
public class CompetitionCourt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CompetitionSession session;

    @Column(name = "court_number", nullable = false)
    private int courtNumber;

    @Column(name = "court_code", nullable = false, length = 4)
    private String courtCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status = CourtStatus.waiting;

    @Enumerated(EnumType.STRING)
    @Column
    private CourtWinner winner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
