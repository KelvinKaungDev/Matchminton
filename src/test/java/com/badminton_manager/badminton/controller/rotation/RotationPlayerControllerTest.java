package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationPlayerRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerStatusUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationSessionResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.enums.RotationScreen;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.exception.BadRequestException;
import com.badminton_manager.badminton.service.rotation.RotationPlayerService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RotationPlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
class RotationPlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RotationPlayerService playerService;

    private UUID sessionId;
    private UUID playerId;
    private RotationStateResponseDTO state;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        playerId = UUID.randomUUID();

        RotationSessionResponseDTO session = new RotationSessionResponseDTO();
        session.setId(sessionId);
        session.setOrganizerId(UUID.randomUUID());
        session.setOrganizerName("Kelvin");
        session.setCourts(5);
        session.setMaxRoundsPerPlayer(6);
        session.setMaxPlayers(36);
        session.setFullRoundPrice(0);
        session.setCourtNames(List.of());
        session.setScreen(RotationScreen.setup);

        RotationPlayerResponseDTO player = new RotationPlayerResponseDTO();
        player.setId(playerId);
        player.setRotationSessionId(sessionId);
        player.setName("Alice");
        player.setSkill(SkillLevel.B);
        player.setStatus(RotationPlayerStatus.waiting);
        player.setRoundsPlayed(0);

        state = new RotationStateResponseDTO();
        state.setSession(session);
        state.setPlayers(List.of(player));
        state.setCourts(List.of());
        state.setHistory(List.of());
    }

    @Test
    void create_validRequest_returns201WithUpdatedState() throws Exception {
        RotationPlayerRequestDTO request = new RotationPlayerRequestDTO();
        request.setRotationSessionId(sessionId);
        request.setName("Alice");
        request.setSkill(SkillLevel.B);

        when(playerService.create(any(RotationPlayerRequestDTO.class))).thenReturn(state);

        mockMvc.perform(post("/api/rotation-players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.players[0].name").value("Alice"))
                .andExpect(jsonPath("$.players[0].status").value("waiting"));
    }

    @Test
    void create_duplicateName_returns400() throws Exception {
        RotationPlayerRequestDTO request = new RotationPlayerRequestDTO();
        request.setRotationSessionId(sessionId);
        request.setName("Alice");

        when(playerService.create(any(RotationPlayerRequestDTO.class)))
                .thenThrow(new BadRequestException("A player named \"Alice\" already exists in this session"));

        mockMvc.perform(post("/api/rotation-players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validRequest_returnsUpdatedState() throws Exception {
        RotationPlayerUpdateDTO request = new RotationPlayerUpdateDTO();
        request.setSkill(SkillLevel.S);

        when(playerService.update(eq(playerId), any(RotationPlayerUpdateDTO.class))).thenReturn(state);

        mockMvc.perform(patch("/api/rotation-players/{id}", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(playerService).update(eq(playerId), any(RotationPlayerUpdateDTO.class));
    }

    @Test
    void updateStatus_validRequest_returnsUpdatedState() throws Exception {
        RotationPlayerStatusUpdateDTO request = new RotationPlayerStatusUpdateDTO();
        request.setStatus(RotationPlayerStatus.bench);

        when(playerService.updateStatus(eq(playerId), any(RotationPlayerStatusUpdateDTO.class))).thenReturn(state);

        mockMvc.perform(patch("/api/rotation-players/{id}/status", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(playerService).updateStatus(eq(playerId), any(RotationPlayerStatusUpdateDTO.class));
    }

    @Test
    void leave_returnsUpdatedState() throws Exception {
        when(playerService.leave(playerId)).thenReturn(state);

        mockMvc.perform(post("/api/rotation-players/{id}/leave", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.session.id").value(sessionId.toString()));
    }

    @Test
    void delete_existingId_returns204() throws Exception {
        doNothing().when(playerService).delete(playerId);

        mockMvc.perform(delete("/api/rotation-players/{id}", playerId))
                .andExpect(status().isNoContent());

        verify(playerService).delete(playerId);
    }
}
