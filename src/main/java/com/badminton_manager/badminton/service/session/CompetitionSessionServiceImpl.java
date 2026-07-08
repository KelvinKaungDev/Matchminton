package com.badminton_manager.badminton.service.session;

import com.badminton_manager.badminton.dto.session.CompetitionSessionRequestDTO;
import com.badminton_manager.badminton.dto.session.CompetitionSessionResponseDTO;
import com.badminton_manager.badminton.enums.SessionStatus;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionSession;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.CompetitionSessionRepository;
import com.badminton_manager.badminton.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CompetitionSessionServiceImpl implements CompetitionSessionService {

    private final CompetitionSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public CompetitionSessionServiceImpl(CompetitionSessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<CompetitionSessionResponseDTO> getAll() {
        return sessionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CompetitionSessionResponseDTO> getByOrganizer(UUID organizerId) {
        return sessionRepository.findByOrganizerId(organizerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CompetitionSessionResponseDTO> getActive() {
        return sessionRepository.findByStatus(SessionStatus.active).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CompetitionSessionResponseDTO getById(UUID id) {
        return sessionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Competition session not found with id: " + id));
    }

    @Override
    public CompetitionSessionResponseDTO create(CompetitionSessionRequestDTO request) {
        User organizer = userRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOrganizerId()));

        CompetitionSession session = new CompetitionSession();
        session.setOrganizer(organizer);
        session.setName(request.getName());
        if (request.getStatus() != null) session.setStatus(request.getStatus());
        if (request.getPointsToWin() != null) session.setPointsToWin(request.getPointsToWin());
        if (request.getNumberOfGames() != null) session.setNumberOfGames(request.getNumberOfGames());
        if (request.getDeuceRule() != null) session.setDeuceRule(request.getDeuceRule());
        if (request.getMaxPoints() != null) session.setMaxPoints(request.getMaxPoints());
        if (request.getCourtCount() != null) session.setCourtCount(request.getCourtCount());

        return toResponse(sessionRepository.save(session));
    }

    @Override
    public CompetitionSessionResponseDTO update(UUID id, CompetitionSessionRequestDTO request) {
        CompetitionSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition session not found with id: " + id));

        if (request.getOrganizerId() != null) {
            User organizer = userRepository.findById(request.getOrganizerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOrganizerId()));
            session.setOrganizer(organizer);
        }
        if (request.getName() != null) session.setName(request.getName());
        if (request.getStatus() != null) session.setStatus(request.getStatus());
        if (request.getPointsToWin() != null) session.setPointsToWin(request.getPointsToWin());
        if (request.getNumberOfGames() != null) session.setNumberOfGames(request.getNumberOfGames());
        if (request.getDeuceRule() != null) session.setDeuceRule(request.getDeuceRule());
        if (request.getMaxPoints() != null) session.setMaxPoints(request.getMaxPoints());
        if (request.getCourtCount() != null) session.setCourtCount(request.getCourtCount());

        return toResponse(sessionRepository.save(session));
    }

    @Override
    public CompetitionSessionResponseDTO finish(UUID id) {
        CompetitionSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition session not found with id: " + id));
        session.setStatus(SessionStatus.finished);
        session.setEndedAt(Instant.now());
        return toResponse(sessionRepository.save(session));
    }

    @Override
    public void delete(UUID id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Competition session not found with id: " + id);
        }
        sessionRepository.deleteById(id);
    }

    private CompetitionSessionResponseDTO toResponse(CompetitionSession session) {
        CompetitionSessionResponseDTO dto = new CompetitionSessionResponseDTO();
        dto.setId(session.getId());
        dto.setOrganizerId(session.getOrganizer().getId());
        dto.setOrganizerName(session.getOrganizer().getName());
        dto.setName(session.getName());
        dto.setStatus(session.getStatus());
        dto.setPointsToWin(session.getPointsToWin());
        dto.setNumberOfGames(session.getNumberOfGames());
        dto.setDeuceRule(session.isDeuceRule());
        dto.setMaxPoints(session.getMaxPoints());
        dto.setCourtCount(session.getCourtCount());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setEndedAt(session.getEndedAt());
        return dto;
    }
}
