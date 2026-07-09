package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationCourtSwapRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.Team;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.RotationCourt;
import com.badminton_manager.badminton.model.RotationPlayer;
import com.badminton_manager.badminton.model.RotationRoundHistoryEntry;
import com.badminton_manager.badminton.model.RotationSession;
import com.badminton_manager.badminton.repository.RotationCourtRepository;
import com.badminton_manager.badminton.repository.RotationPlayerRepository;
import com.badminton_manager.badminton.repository.RotationRoundHistoryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RotationCourtServiceImpl implements RotationCourtService {

    private final RotationCourtRepository courtRepository;
    private final RotationPlayerRepository playerRepository;
    private final RotationRoundHistoryEntryRepository historyRepository;
    private final RotationMatcher matcher;
    private final RotationStateAssembler assembler;

    public RotationCourtServiceImpl(RotationCourtRepository courtRepository,
                                     RotationPlayerRepository playerRepository,
                                     RotationRoundHistoryEntryRepository historyRepository,
                                     RotationMatcher matcher,
                                     RotationStateAssembler assembler) {
        this.courtRepository = courtRepository;
        this.playerRepository = playerRepository;
        this.historyRepository = historyRepository;
        this.matcher = matcher;
        this.assembler = assembler;
    }

    @Override
    @Transactional
    public RotationStateResponseDTO complete(UUID id) {
        RotationCourt court = getOrThrow(id);
        RotationSession session = court.getRotationSession();
        List<RotationPlayer> seated = playerRepository.findByCourtId(id);
        List<RotationPlayer> teamA = seated.stream().filter(p -> p.getTeam() == Team.teamA).toList();
        List<RotationPlayer> teamB = seated.stream().filter(p -> p.getTeam() == Team.teamB).toList();

        if (!seated.isEmpty()) {
            int roundNumber = historyRepository.findByRotationSessionIdOrderByRoundNumberAsc(session.getId()).size() + 1;
            RotationRoundHistoryEntry entry = new RotationRoundHistoryEntry();
            entry.setRotationSession(session);
            entry.setRoundNumber(roundNumber);
            entry.setCourtNumber(court.getCourtNumber());
            entry.setCourtName(court.getName() != null ? court.getName() : String.valueOf(court.getCourtNumber()));
            entry.setTeamANamesCsv(assembler.encodeNames(teamA));
            entry.setTeamASkillsCsv(assembler.encodeSkills(teamA));
            entry.setTeamBNamesCsv(assembler.encodeNames(teamB));
            entry.setTeamBSkillsCsv(assembler.encodeSkills(teamB));
            historyRepository.save(entry);
        }

        for (RotationPlayer player : seated) {
            player.setStatus(player.getRoundsPlayed() >= session.getMaxRoundsPerPlayer()
                    ? RotationPlayerStatus.done
                    : RotationPlayerStatus.bench);
            player.setCourt(null);
            player.setTeam(null);
        }
        playerRepository.saveAll(seated);

        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO refill(UUID id) {
        RotationCourt court = getOrThrow(id);
        RotationSession session = court.getRotationSession();
        List<RotationPlayer> allPlayers = playerRepository.findByRotationSessionId(session.getId());
        List<RotationMatcher.MatchGroup> groups = matcher.generateRound(allPlayers, 1);

        if (!groups.isEmpty()) {
            RotationMatcher.MatchGroup group = groups.get(0);
            seatTeam(group.teamA(), court, Team.teamA);
            seatTeam(group.teamB(), court, Team.teamB);
        }

        return assembler.build(session);
    }

    @Override
    @Transactional
    public RotationStateResponseDTO swap(UUID id, RotationCourtSwapRequestDTO request) {
        RotationCourt court = getOrThrow(id);
        RotationSession session = court.getRotationSession();

        RotationPlayer courtPlayer = playerRepository.findById(request.getCourtPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Rotation player not found with id: " + request.getCourtPlayerId()));
        RotationPlayer benchPlayer = playerRepository.findById(request.getBenchPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Rotation player not found with id: " + request.getBenchPlayerId()));

        Team team = courtPlayer.getTeam();
        benchPlayer.setStatus(RotationPlayerStatus.playing);
        benchPlayer.setRoundsPlayed(benchPlayer.getRoundsPlayed() + 1);
        benchPlayer.setCourt(court);
        benchPlayer.setTeam(team);
        playerRepository.save(benchPlayer);

        courtPlayer.setStatus(RotationPlayerStatus.bench);
        courtPlayer.setCourt(null);
        courtPlayer.setTeam(null);
        playerRepository.save(courtPlayer);

        return assembler.build(session);
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

    private RotationCourt getOrThrow(UUID id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rotation court not found with id: " + id));
    }
}
