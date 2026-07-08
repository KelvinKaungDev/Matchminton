package com.badminton_manager.badminton.service.session;

import com.badminton_manager.badminton.dto.session.CompetitionSessionRequestDTO;
import com.badminton_manager.badminton.dto.session.CompetitionSessionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CompetitionSessionService {
    List<CompetitionSessionResponseDTO> getAll();
    List<CompetitionSessionResponseDTO> getByOrganizer(UUID organizerId);
    List<CompetitionSessionResponseDTO> getActive();
    CompetitionSessionResponseDTO getById(UUID id);
    CompetitionSessionResponseDTO create(CompetitionSessionRequestDTO request);
    CompetitionSessionResponseDTO update(UUID id, CompetitionSessionRequestDTO request);
    CompetitionSessionResponseDTO finish(UUID id);
    void delete(UUID id);
}
