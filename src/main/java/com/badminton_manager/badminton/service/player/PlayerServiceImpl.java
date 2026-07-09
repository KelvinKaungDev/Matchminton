package com.badminton_manager.badminton.service.player;

import com.badminton_manager.badminton.dto.player.PlayerRequestDTO;
import com.badminton_manager.badminton.dto.player.PlayerResponseDTO;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionCourt;
import com.badminton_manager.badminton.model.Player;
import com.badminton_manager.badminton.repository.CompetitionCourtRepository;
import com.badminton_manager.badminton.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final CompetitionCourtRepository courtRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository, CompetitionCourtRepository courtRepository) {
        this.playerRepository = playerRepository;
        this.courtRepository = courtRepository;
    }

    @Override
    public List<PlayerResponseDTO> getByCourt(UUID courtId) {
        return playerRepository.findByCourtId(courtId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PlayerResponseDTO getById(UUID id) {
        return playerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
    }

    @Override
    public PlayerResponseDTO create(PlayerRequestDTO request) {
        CompetitionCourt court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));

        Player player = new Player();
        player.setCourt(court);
        player.setName(request.getName());
        player.setTeam(request.getTeam());
        player.setSkill(request.getSkill() != null ? request.getSkill() : SkillLevel.B);

        return toResponse(playerRepository.save(player));
    }

    @Override
    public PlayerResponseDTO update(UUID id, PlayerRequestDTO request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        if (request.getCourtId() != null) {
            CompetitionCourt court = courtRepository.findById(request.getCourtId())
                    .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + request.getCourtId()));
            player.setCourt(court);
        }
        if (request.getName() != null) player.setName(request.getName());
        if (request.getTeam() != null) player.setTeam(request.getTeam());
        if (request.getSkill() != null) player.setSkill(request.getSkill());

        return toResponse(playerRepository.save(player));
    }

    @Override
    public void delete(UUID id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Player not found with id: " + id);
        }
        playerRepository.deleteById(id);
    }

    private PlayerResponseDTO toResponse(Player player) {
        PlayerResponseDTO dto = new PlayerResponseDTO();
        dto.setId(player.getId());
        dto.setCourtId(player.getCourt().getId());
        dto.setCourtNumber(player.getCourt().getCourtNumber());
        dto.setName(player.getName());
        dto.setTeam(player.getTeam());
        dto.setSkill(player.getSkill());
        dto.setCreatedAt(player.getCreatedAt());
        return dto;
    }
}
