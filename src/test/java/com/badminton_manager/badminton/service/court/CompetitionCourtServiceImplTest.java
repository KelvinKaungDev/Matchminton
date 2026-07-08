package com.badminton_manager.badminton.service.court;

import com.badminton_manager.badminton.dto.court.CompetitionCourtRequestDTO;
import com.badminton_manager.badminton.dto.court.CompetitionCourtResponseDTO;
import com.badminton_manager.badminton.enums.CourtStatus;
import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionCourt;
import com.badminton_manager.badminton.model.CompetitionSession;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.CompetitionCourtRepository;
import com.badminton_manager.badminton.repository.CompetitionSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionCourtServiceImplTest {

    @Mock
    private CompetitionCourtRepository courtRepository;

    @Mock
    private CompetitionSessionRepository sessionRepository;

    @InjectMocks
    private CompetitionCourtServiceImpl courtService;

    private CompetitionSession session;
    private CompetitionCourt court;
    private UUID sessionId;
    private UUID courtId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        courtId = UUID.randomUUID();

        User organizer = new User();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Kelvin");

        session = new CompetitionSession();
        session.setId(sessionId);
        session.setOrganizer(organizer);
        session.setName("Sunday Tournament");

        court = new CompetitionCourt();
        court.setId(courtId);
        court.setSession(session);
        court.setCourtNumber(1);
        court.setCourtCode("A1B2");
        court.setStatus(CourtStatus.waiting);
        court.setCreatedAt(Instant.now());
    }

    @Test
    void getBySession_returnsCourtsForSession() {
        when(courtRepository.findBySessionId(sessionId)).thenReturn(List.of(court));

        List<CompetitionCourtResponseDTO> result = courtService.getBySession(sessionId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourtNumber()).isEqualTo(1);
        assertThat(result.get(0).getCourtCode()).isEqualTo("A1B2");
    }

    @Test
    void getById_existingId_returnsCourt() {
        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));

        CompetitionCourtResponseDTO result = courtService.getById(courtId);

        assertThat(result.getId()).isEqualTo(courtId);
        assertThat(result.getSessionName()).isEqualTo("Sunday Tournament");
        assertThat(result.getStatus()).isEqualTo(CourtStatus.waiting);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(courtRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void getByCourtCode_existingCode_returnsCourt() {
        when(courtRepository.findByCourtCode("A1B2")).thenReturn(Optional.of(court));

        CompetitionCourtResponseDTO result = courtService.getByCourtCode("A1B2");

        assertThat(result.getCourtCode()).isEqualTo("A1B2");
    }

    @Test
    void getByCourtCode_notFound_throwsResourceNotFoundException() {
        when(courtRepository.findByCourtCode("XXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.getByCourtCode("XXXX"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("XXXX");
    }

    @Test
    void create_validRequest_savesAndReturnsCourt() {
        CompetitionCourtRequestDTO request = new CompetitionCourtRequestDTO();
        request.setSessionId(sessionId);
        request.setCourtNumber(1);
        request.setCourtCode("A1B2");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(courtRepository.save(any(CompetitionCourt.class))).thenReturn(court);

        CompetitionCourtResponseDTO result = courtService.create(request);

        assertThat(result.getCourtCode()).isEqualTo("A1B2");
        assertThat(result.getSessionName()).isEqualTo("Sunday Tournament");
        verify(courtRepository).save(any(CompetitionCourt.class));
    }

    @Test
    void create_sessionNotFound_throwsResourceNotFoundException() {
        UUID unknownSessionId = UUID.randomUUID();
        CompetitionCourtRequestDTO request = new CompetitionCourtRequestDTO();
        request.setSessionId(unknownSessionId);

        when(sessionRepository.findById(unknownSessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courtRepository, never()).save(any());
    }

    @Test
    void finishCourt_setsStatusFinishedAndWinner() {
        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));
        when(courtRepository.save(any(CompetitionCourt.class))).thenAnswer(inv -> inv.getArgument(0));

        CompetitionCourtResponseDTO result = courtService.finishCourt(courtId, CourtWinner.teamA);

        assertThat(result.getStatus()).isEqualTo(CourtStatus.finished);
        assertThat(result.getWinner()).isEqualTo(CourtWinner.teamA);
    }

    @Test
    void finishCourt_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(courtRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.finishCourt(unknownId, CourtWinner.teamB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesCourt() {
        when(courtRepository.existsById(courtId)).thenReturn(true);

        courtService.delete(courtId);

        verify(courtRepository).deleteById(courtId);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(courtRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> courtService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courtRepository, never()).deleteById(any());
    }
}
