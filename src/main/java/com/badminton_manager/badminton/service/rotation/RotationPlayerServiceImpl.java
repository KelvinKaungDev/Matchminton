package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationPlayerRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerStatusUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.enums.Team;
import com.badminton_manager.badminton.exception.BadRequestException;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.RotationCourt;
import com.badminton_manager.badminton.model.RotationPlayer;
import com.badminton_manager.badminton.model.RotationSession;
import com.badminton_manager.badminton.repository.RotationPlayerRepository;
import com.badminton_manager.badminton.repository.RotationSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RotationPlayerServiceImpl implements RotationPlayerService {

    private final RotationPlayerRepository playerRepository;
    private final RotationSessionRepository sessionRepository;
    private final RotationMatcher matcher;
    private final RotationStateAssembler assembler;

    public RotationPlayerServiceImpl(RotationPlayerRepository playerRepository,
                                      RotationSessionRepository sessionRepository,
                                      RotationMatcher matcher,
                                      RotationStateAssembler assembler) {
        this.playerRepository = playerRepository;
        this.sessionRepository = sessionRepository;
        this.matcher = matcher;
        this.assembler = assembler;
    }

    @Override
    public RotationStateResponseDTO create(RotationPlayerRequestDTO request) {
        RotationSession session = sessionRepository.findById(request.getRotationSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Rotation session not found with id: " + request.getRotationSessionId()));

        if (playerRepository.existsByRotationSessionIdAndNameIgnoreCase(session.getId(), request.getName())) {
            throw new BadRequestException("A player named \"" + request.getName() + "\" already exists in this session");
        }
        long currentCount = playerRepository.findByRotationSessionId(session.getId()).size();
        if (currentCount >= session.getMaxPlayers()) {
            throw new BadRequestException("Session is full (max " + session.getMaxPlayers() + " players)");
        }

        RotationPlayer player = new RotationPlayer();
        player.setRotationSession(session);
        player.setName(request.getName());
        player.setSkill(request.getSkill() != null ? request.getSkill() : SkillLevel.B);
        player.setStatus(request.getStatus() != null ? request.getStatus() : RotationPlayerStatus.waiting);
        playerRepository.save(player);

        return assembler.build(session);
    }

    @Override
    public RotationStateResponseDTO update(UUID id, RotationPlayerUpdateDTO request) {
        RotationPlayer player = getOrThrow(id);
        if (request.getSkill() != null) player.setSkill(request.getSkill());
        playerRepository.save(player);
        return assembler.build(player.getRotationSession());
    }

    @Override
    public RotationStateResponseDTO updateStatus(UUID id, RotationPlayerStatusUpdateDTO request) {
        RotationPlayer player = getOrThrow(id);
        if (request.getStatus() != null) player.setStatus(request.getStatus());
        playerRepository.save(player);
        return assembler.build(player.getRotationSession());
    }

    @Override
    @Transactional
    public RotationStateResponseDTO leave(UUID id) {
        RotationPlayer player = getOrThrow(id);
        RotationSession session = player.getRotationSession();
        RotationPlayerStatus previousStatus = player.getStatus();

        if (previousStatus == RotationPlayerStatus.playing) {
            RotationCourt court = player.getCourt();
            Team team = player.getTeam();
            player.setCourt(null);
            player.setTeam(null);
            player.setStatus(RotationPlayerStatus.leave);
            playerRepository.save(player);

            List<RotationPlayer> others = playerRepository.findByRotationSessionId(session.getId()).stream()
                    .filter(p -> !p.getId().equals(id))
                    .toList();
            RotationPlayer substitute = matcher.findBestSub(others);
            if (substitute != null) {
                substitute.setStatus(RotationPlayerStatus.playing);
                substitute.setRoundsPlayed(substitute.getRoundsPlayed() + 1);
                substitute.setCourt(court);
                substitute.setTeam(team);
                playerRepository.save(substitute);
            }
        } else {
            player.setStatus(RotationPlayerStatus.leave);
            playerRepository.save(player);
        }

        return assembler.build(session);
    }

    @Override
    public void delete(UUID id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rotation player not found with id: " + id);
        }
        playerRepository.deleteById(id);
    }

    private RotationPlayer getOrThrow(UUID id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rotation player not found with id: " + id));
    }
}
