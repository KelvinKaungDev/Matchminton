package com.badminton_manager.badminton.service.session;

import com.badminton_manager.badminton.dto.session.CompetitionSessionRequestDTO;
import com.badminton_manager.badminton.dto.session.CompetitionSessionResponseDTO;
import com.badminton_manager.badminton.enums.SessionStatus;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.CompetitionSession;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.CompetitionSessionRepository;
import com.badminton_manager.badminton.repository.UserRepository;
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
class CompetitionSessionServiceImplTest {

    @Mock
    private CompetitionSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompetitionSessionServiceImpl sessionService;

    private User organizer;
    private CompetitionSession session;
    private UUID organizerId;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        organizer = new User();
        organizer.setId(organizerId);
        organizer.setName("Kelvin");

        session = new CompetitionSession();
        session.setId(sessionId);
        session.setOrganizer(organizer);
        session.setName("Sunday Tournament");
        session.setStatus(SessionStatus.active);
        session.setPointsToWin(21);
        session.setNumberOfGames(3);
        session.setDeuceRule(true);
        session.setMaxPoints(30);
        session.setCourtCount(4);
        session.setCreatedAt(Instant.now());
    }

    @Test
    void getAll_returnsAllSessions() {
        when(sessionRepository.findAll()).thenReturn(List.of(session));

        List<CompetitionSessionResponseDTO> result = sessionService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sunday Tournament");
    }

    @Test
    void getByOrganizer_returnsSessionsForOrganizer() {
        when(sessionRepository.findByOrganizerId(organizerId)).thenReturn(List.of(session));

        List<CompetitionSessionResponseDTO> result = sessionService.getByOrganizer(organizerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganizerName()).isEqualTo("Kelvin");
    }

    @Test
    void getActive_returnsOnlyActiveSessions() {
        when(sessionRepository.findByStatus(SessionStatus.active)).thenReturn(List.of(session));

        List<CompetitionSessionResponseDTO> result = sessionService.getActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SessionStatus.active);
    }

    @Test
    void getById_existingId_returnsSession() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        CompetitionSessionResponseDTO result = sessionService.getById(sessionId);

        assertThat(result.getId()).isEqualTo(sessionId);
        assertThat(result.getPointsToWin()).isEqualTo(21);
        assertThat(result.getNumberOfGames()).isEqualTo(3);
        assertThat(result.isDeuceRule()).isTrue();
        assertThat(result.getCourtCount()).isEqualTo(4);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(sessionRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void create_validRequest_savesAndReturnsSession() {
        CompetitionSessionRequestDTO request = new CompetitionSessionRequestDTO();
        request.setOrganizerId(organizerId);
        request.setName("Sunday Tournament");
        request.setCourtCount(4);

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(sessionRepository.save(any(CompetitionSession.class))).thenReturn(session);

        CompetitionSessionResponseDTO result = sessionService.create(request);

        assertThat(result.getName()).isEqualTo("Sunday Tournament");
        assertThat(result.getOrganizerName()).isEqualTo("Kelvin");
        verify(sessionRepository).save(any(CompetitionSession.class));
    }

    @Test
    void create_organizerNotFound_throwsResourceNotFoundException() {
        UUID unknownOrganizerId = UUID.randomUUID();
        CompetitionSessionRequestDTO request = new CompetitionSessionRequestDTO();
        request.setOrganizerId(unknownOrganizerId);

        when(userRepository.findById(unknownOrganizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void create_withCustomRules_appliesCustomValues() {
        CompetitionSessionRequestDTO request = new CompetitionSessionRequestDTO();
        request.setOrganizerId(organizerId);
        request.setName("Quick Match");
        request.setPointsToWin(15);
        request.setNumberOfGames(1);
        request.setDeuceRule(false);
        request.setMaxPoints(15);
        request.setCourtCount(2);

        CompetitionSession customSession = new CompetitionSession();
        customSession.setId(UUID.randomUUID());
        customSession.setOrganizer(organizer);
        customSession.setName("Quick Match");
        customSession.setPointsToWin(15);
        customSession.setNumberOfGames(1);
        customSession.setDeuceRule(false);
        customSession.setMaxPoints(15);
        customSession.setCourtCount(2);
        customSession.setStatus(SessionStatus.active);

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(sessionRepository.save(any(CompetitionSession.class))).thenReturn(customSession);

        CompetitionSessionResponseDTO result = sessionService.create(request);

        assertThat(result.getPointsToWin()).isEqualTo(15);
        assertThat(result.getNumberOfGames()).isEqualTo(1);
        assertThat(result.isDeuceRule()).isFalse();
        assertThat(result.getCourtCount()).isEqualTo(2);
    }

    @Test
    void finish_existingId_setsStatusFinishedAndEndedAt() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(CompetitionSession.class))).thenAnswer(inv -> inv.getArgument(0));

        CompetitionSessionResponseDTO result = sessionService.finish(sessionId);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.finished);
        assertThat(result.getEndedAt()).isNotNull();
    }

    @Test
    void finish_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(sessionRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.finish(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesSession() {
        when(sessionRepository.existsById(sessionId)).thenReturn(true);

        sessionService.delete(sessionId);

        verify(sessionRepository).deleteById(sessionId);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(sessionRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> sessionService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(sessionRepository, never()).deleteById(any());
    }
}
