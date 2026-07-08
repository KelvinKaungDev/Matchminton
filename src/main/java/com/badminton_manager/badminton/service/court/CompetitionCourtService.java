package com.badminton_manager.badminton.service.court;

import com.badminton_manager.badminton.dto.court.CompetitionCourtRequestDTO;
import com.badminton_manager.badminton.dto.court.CompetitionCourtResponseDTO;
import com.badminton_manager.badminton.enums.CourtWinner;

import java.util.List;
import java.util.UUID;

public interface CompetitionCourtService {
    List<CompetitionCourtResponseDTO> getBySession(UUID sessionId);
    CompetitionCourtResponseDTO getById(UUID id);
    CompetitionCourtResponseDTO getByCourtCode(String courtCode);
    CompetitionCourtResponseDTO create(CompetitionCourtRequestDTO request);
    CompetitionCourtResponseDTO update(UUID id, CompetitionCourtRequestDTO request);
    CompetitionCourtResponseDTO finishCourt(UUID id, CourtWinner winner);
    void delete(UUID id);
}
