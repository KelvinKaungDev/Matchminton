package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationConfigRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationScreenUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.RotationScreen;
import com.badminton_manager.badminton.enums.Team;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.RotationCourt;
import com.badminton_manager.badminton.model.RotationPlayer;
import com.badminton_manager.badminton.model.RotationSession;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.RotationCourtRepository;
import com.badminton_manager.badminton.repository.RotationPlayerRepository;
import com.badminton_manager.badminton.repository.RotationRoundHistoryEntryRepository;
import com.badminton_manager.badminton.repository.RotationSessionRepository;
import com.badminton_manager.badminton.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RotationSessionServiceImpl implements RotationSessionService {

    private final RotationSessionRepository sessionRepository;
    private final RotationPlayerRepository playerRepository;
    private final RotationCourtRepository courtRepository;
    private final RotationRoundHistoryEntryRepository historyRepository;
    private final UserRepository userRepository;
    private final RotationMatcher matcher;
    private final RotationStateAssembler assembler;

    public RotationSessionServiceImpl(RotationSessionRepository sessionRepository,
                                       RotationPlayerRepository playerRepository,
                                       RotationCourtRepository courtRepository,
                                       RotationRoundHistoryEntryRepository historyRepository,
                                       UserRepository userRepository,
                                       RotationMatcher matcher,
                                       RotationStateAssembler assembler) {
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
        this.courtRepository = courtRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.matcher = matcher;
        this.assembler = assembler;
    }

    @Override
    public RotationStateResponseDTO getStateByOrganizer(UUID organizerId) {
        RotationSession session = sessionRepository.findByOrganizerId(organizerId)
                .orElseGet(() -> getOrCreateForOrganizer(organizerId));
        return assembler.build(session);
    }

    /**
     * Two near-simultaneous first-load requests for the same organizer can both miss the
     * findByOrganizerId check above and both attempt to create a session. Each save() here
     * runs as its own auto-committing unit of work (no surrounding @Transactional on this
     * method), so a unique-constraint failure only aborts this one insert — the loser just
     * re-reads the row the winner committed instead of poisoning a shared transaction.
     */
    private RotationSession getOrCreateForOrganizer(UUID organizerId) {
        try {
            return createForOrganizer(organizerId);
        } catch (DataIntegrityViolationException e) {
            return sessionRepository.findByOrganizerId(organizerId).orElseThrow(() -> e);
        }
    }

    private RotationSession createForOrganizer(UUID organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + organizerId));
        RotationSession session = new RotationSession();
        session.setOrganizer(organizer);
        return sessionRepository.save(session);
    }

    @Override
    public RotationStateResponseDTO updateConfig(UUID id, RotationConfigRequestDTO request) {
        RotationSession session = getOrThrow(id);
        if (request.getCourts() != null) session.setCourts(request.getCourts());
        if (request.getMaxRoundsPerPlayer() != null) session.setMaxRoundsPerPlayer(request.getMaxRoundsPerPlayer());
        if (request.getMaxPlayers() != null) session.setMaxPlayers(request.getMaxPlayers());
        if (request.getFullRoundPrice() != null) session.setFullRoundPrice(request.getFullRoundPrice());
        if (request.getCourtNames() != null) session.setCourtNamesCsv(assembler.encodeStringList(request.getCourtNames()));
        sessionRepository.save(session);
        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO markAllBench(UUID id) {
        RotationSession session = getOrThrow(id);
        List<RotationPlayer> waiting = playerRepository.findByRotationSessionIdAndStatus(id, RotationPlayerStatus.waiting);
        for (RotationPlayer player : waiting) {
            player.setStatus(RotationPlayerStatus.bench);
        }
        playerRepository.saveAll(waiting);
        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO start(UUID id) {
        RotationSession session = getOrThrow(id);
        List<RotationPlayer> allPlayers = playerRepository.findByRotationSessionId(id);

        historyRepository.deleteByRotationSessionId(id);
        List<RotationCourt> existingCourts = courtRepository.findByRotationSessionId(id);
        for (RotationPlayer player : allPlayers) {
            player.setCourt(null);
            player.setTeam(null);
        }
        playerRepository.saveAll(allPlayers);
        courtRepository.deleteAll(existingCourts);

        List<RotationMatcher.MatchGroup> groups = matcher.generateRound(allPlayers, session.getCourts());
        seatGroups(session, groups, 0);

        session.setScreen(RotationScreen.session);
        sessionRepository.save(session);
        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO fillEmptyCourts(UUID id) {
        RotationSession session = getOrThrow(id);
        int existingCourtCount = courtRepository.findByRotationSessionId(id).size();
        int slotsToFill = session.getCourts() - existingCourtCount;
        if (slotsToFill <= 0) {
            return assembler.build(session);
        }

        List<RotationPlayer> allPlayers = playerRepository.findByRotationSessionId(id);
        List<RotationMatcher.MatchGroup> groups = matcher.generateRound(allPlayers, slotsToFill);
        seatGroups(session, groups, existingCourtCount);
        return assembler.build(session);
    }

    private void seatGroups(RotationSession session, List<RotationMatcher.MatchGroup> groups, int courtNumberOffset) {
        int courtNumber = courtNumberOffset;
        for (RotationMatcher.MatchGroup group : groups) {
            courtNumber++;
            RotationCourt court = new RotationCourt();
            court.setRotationSession(session);
            court.setCourtNumber(courtNumber);
            court.setName(assembler.resolveCourtName(session, courtNumber));
            court = courtRepository.save(court);
            seatTeam(group.teamA(), court, Team.teamA);
            seatTeam(group.teamB(), court, Team.teamB);
        }
    }

    private void seatTeam(List<RotationPlayer> team, RotationCourt court, Team side) {
        for (RotationPlayer player : team) {
            player.setStatus(RotationPlayerStatus.playing);
            player.setCourt(court);
            player.setTeam(side);
            player.setRoundsPlayed(player.getRoundsPlayed() + 1);
        }
        playerRepository.saveAll(team);
    }

    @Override
    public RotationStateResponseDTO updateScreen(UUID id, RotationScreenUpdateDTO request) {
        RotationSession session = getOrThrow(id);
        if (request.getScreen() != null) session.setScreen(request.getScreen());
        sessionRepository.save(session);
        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO reset(UUID id) {
        RotationSession session = getOrThrow(id);
        playerRepository.deleteAll(playerRepository.findByRotationSessionId(id));
        courtRepository.deleteAll(courtRepository.findByRotationSessionId(id));
        historyRepository.deleteByRotationSessionId(id);

        session.setCourts(5);
        session.setMaxRoundsPerPlayer(6);
        session.setMaxPlayers(36);
        session.setFullRoundPrice(0);
        session.setCourtNamesCsv(null);
        session.setScreen(RotationScreen.setup);
        sessionRepository.save(session);
        return assembler.build(session);
    }

    private RotationSession getOrThrow(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rotation session not found with id: " + id));
    }
}
