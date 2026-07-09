package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationConfigRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationScreenUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationSessionResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationScreen;
import com.badminton_manager.badminton.service.rotation.RotationSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RotationSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RotationSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RotationSessionService sessionService;

    private UUID sessionId;
    private UUID organizerId;
    private RotationStateResponseDTO state;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        organizerId = UUID.randomUUID();

        RotationSessionResponseDTO session = new RotationSessionResponseDTO();
        session.setId(sessionId);
        session.setOrganizerId(organizerId);
        session.setOrganizerName("Kelvin");
        session.setCourts(5);
        session.setMaxRoundsPerPlayer(6);
        session.setMaxPlayers(36);
        session.setFullRoundPrice(0);
        session.setCourtNames(List.of());
        session.setScreen(RotationScreen.setup);

        state = new RotationStateResponseDTO();
        state.setSession(session);
        state.setPlayers(List.of());
        state.setCourts(List.of());
        state.setHistory(List.of());
    }

    @Test
    void getStateByOrganizer_returnsAggregateState() throws Exception {
        when(sessionService.getStateByOrganizer(organizerId)).thenReturn(state);

        mockMvc.perform(get("/api/rotation-sessions/organizer/{organizerId}", organizerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.session.screen").value("setup"))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.courts").isArray())
                .andExpect(jsonPath("$.history").isArray());
    }

    @Test
    void updateConfig_validRequest_returnsUpdatedState() throws Exception {
        RotationConfigRequestDTO request = new RotationConfigRequestDTO();
        request.setCourts(3);
        request.setMaxRoundsPerPlayer(4);
        request.setMaxPlayers(20);
        request.setFullRoundPrice(500);
        request.setCourtNames(List.of("North", "South", "East"));

        when(sessionService.updateConfig(eq(sessionId), any(RotationConfigRequestDTO.class))).thenReturn(state);

        mockMvc.perform(put("/api/rotation-sessions/{id}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));

        verify(sessionService).updateConfig(eq(sessionId), any(RotationConfigRequestDTO.class));
    }

    @Test
    void markAllBench_returnsUpdatedState() throws Exception {
        when(sessionService.markAllBench(sessionId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-sessions/{id}/mark-all-bench", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void start_returnsUpdatedState() throws Exception {
        when(sessionService.start(sessionId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-sessions/{id}/start", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void fillEmptyCourts_returnsUpdatedState() throws Exception {
        when(sessionService.fillEmptyCourts(sessionId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-sessions/{id}/fill-empty-courts", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void updateScreen_validRequest_returnsUpdatedState() throws Exception {
        RotationScreenUpdateDTO request = new RotationScreenUpdateDTO();
        request.setScreen(RotationScreen.summary);

        when(sessionService.updateScreen(eq(sessionId), any(RotationScreenUpdateDTO.class))).thenReturn(state);

        mockMvc.perform(patch("/api/rotation-sessions/{id}/screen", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(sessionService).updateScreen(eq(sessionId), any(RotationScreenUpdateDTO.class));
    }

    @Test
    void reset_returnsResetState() throws Exception {
        when(sessionService.reset(sessionId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-sessions/{id}/reset", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }
}
