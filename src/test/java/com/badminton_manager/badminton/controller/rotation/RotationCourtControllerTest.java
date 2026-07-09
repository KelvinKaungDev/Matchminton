package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationCourtSwapRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationSessionResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationScreen;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.service.rotation.RotationCourtService;
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

@WebMvcTest(RotationCourtController.class)
@AutoConfigureMockMvc(addFilters = false)
class RotationCourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RotationCourtService courtService;

    private UUID sessionId;
    private UUID courtId;
    private RotationStateResponseDTO state;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        courtId = UUID.randomUUID();

        RotationSessionResponseDTO session = new RotationSessionResponseDTO();
        session.setId(sessionId);
        session.setOrganizerId(UUID.randomUUID());
        session.setOrganizerName("Kelvin");
        session.setCourts(5);
        session.setMaxRoundsPerPlayer(6);
        session.setMaxPlayers(36);
        session.setFullRoundPrice(0);
        session.setCourtNames(List.of());
        session.setScreen(RotationScreen.session);

        state = new RotationStateResponseDTO();
        state.setSession(session);
        state.setPlayers(List.of());
        state.setCourts(List.of());
        state.setHistory(List.of());
    }

    @Test
    void complete_returnsUpdatedState() throws Exception {
        when(courtService.complete(courtId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-courts/{id}/complete", courtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void complete_notFound_returns404() throws Exception {
        when(courtService.complete(courtId))
                .thenThrow(new ResourceNotFoundException("Rotation court not found with id: " + courtId));

        mockMvc.perform(post("/api/rotation-courts/{id}/complete", courtId))
                .andExpect(status().isNotFound());
    }

    @Test
    void refill_returnsUpdatedState() throws Exception {
        when(courtService.refill(courtId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-courts/{id}/refill", courtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void swap_validRequest_returnsUpdatedState() throws Exception {
        RotationCourtSwapRequestDTO request = new RotationCourtSwapRequestDTO();
        request.setCourtPlayerId(UUID.randomUUID());
        request.setBenchPlayerId(UUID.randomUUID());

        when(courtService.swap(eq(courtId), any(RotationCourtSwapRequestDTO.class))).thenReturn(state);

        mockMvc.perform(post("/api/rotation-courts/{id}/swap", courtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(courtService).swap(eq(courtId), any(RotationCourtSwapRequestDTO.class));
    }
}
