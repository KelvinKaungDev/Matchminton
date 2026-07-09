package com.badminton_manager.badminton.service.court;

import com.badminton_manager.badminton.dto.court.CompetitionCourtRequestDTO;
import com.badminton_manager.badminton.dto.court.CompetitionCourtResponseDTO;
import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionCourt;
import com.badminton_manager.badminton.model.CompetitionSession;
import com.badminton_manager.badminton.repository.CompetitionCourtRepository;
import com.badminton_manager.badminton.repository.CompetitionSessionRepository;
import com.badminton_manager.badminton.repository.GameRepository;
import com.badminton_manager.badminton.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompetitionCourtServiceImpl implements CompetitionCourtService {

    private final CompetitionCourtRepository courtRepository;
    private final CompetitionSessionRepository sessionRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public CompetitionCourtServiceImpl(CompetitionCourtRepository courtRepository,
                                       CompetitionSessionRepository sessionRepository,
                                       GameRepository gameRepository,
                                       PlayerRepository playerRepository) {
        this.courtRepository = courtRepository;
        this.sessionRepository = sessionRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public List<CompetitionCourtResponseDTO> getBySession(UUID sessionId) {
        return courtRepository.findBySessionId(sessionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CompetitionCourtResponseDTO getById(UUID id) {
        return courtRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
    }

    @Override
    public CompetitionCourtResponseDTO getByCourtCode(String courtCode) {
        return courtRepository.findByCourtCode(courtCode)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with code: " + courtCode));
    }

    @Override
    public CompetitionCourtResponseDTO create(CompetitionCourtRequestDTO request) {
        CompetitionSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));

        CompetitionCourt court = new CompetitionCourt();
        court.setSession(session);
        court.setCourtNumber(request.getCourtNumber());
        court.setCourtCode(request.getCourtCode());
        if (request.getStatus() != null) court.setStatus(request.getStatus());
        court.setWinner(request.getWinner());

        return toResponse(courtRepository.save(court));
    }

    @Override
    public CompetitionCourtResponseDTO update(UUID id, CompetitionCourtRequestDTO request) {
        CompetitionCourt court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));

        if (request.getSessionId() != null) {
            CompetitionSession session = sessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));
            court.setSession(session);
        }
        if (request.getCourtCode() != null) court.setCourtCode(request.getCourtCode());
        if (request.getStatus() != null) court.setStatus(request.getStatus());
        court.setCourtNumber(request.getCourtNumber());
        court.setWinner(request.getWinner());

        return toResponse(courtRepository.save(court));
    }

    @Override
    public CompetitionCourtResponseDTO finishCourt(UUID id, CourtWinner winner) {
        CompetitionCourt court = courtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
        court.setStatus(CourtStatus.finished);
        court.setWinner(winner);
        return toResponse(courtRepository.save(court));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!courtRepository.existsById(id)) {
            throw new ResourceNotFoundException("Court not found with id: " + id);
        }
        gameRepository.deleteAll(gameRepository.findByCourtId(id));
        playerRepository.deleteAll(playerRepository.findByCourtId(id));
        courtRepository.deleteById(id);
    }

    private CompetitionCourtResponseDTO toResponse(CompetitionCourt court) {
        CompetitionCourtResponseDTO dto = new CompetitionCourtResponseDTO();
        dto.setId(court.getId());
        dto.setSessionId(court.getSession().getId());
        dto.setSessionName(court.getSession().getName());
        dto.setCourtNumber(court.getCourtNumber());
        dto.setCourtCode(court.getCourtCode());
        dto.setStatus(court.getStatus());
        dto.setWinner(court.getWinner());
        dto.setCreatedAt(court.getCreatedAt());
        return dto;
    }
}
