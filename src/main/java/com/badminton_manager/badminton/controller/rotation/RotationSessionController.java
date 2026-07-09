package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationConfigRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationScreenUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.service.rotation.RotationSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Rotation Sessions")
@RestController
@RequestMapping("/api/rotation-sessions")
public class RotationSessionController {

    private final RotationSessionService sessionService;

    public RotationSessionController(RotationSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<RotationStateResponseDTO> getStateByOrganizer(@PathVariable UUID organizerId) {
        return ResponseEntity.ok(sessionService.getStateByOrganizer(organizerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RotationStateResponseDTO> updateConfig(@PathVariable UUID id, @RequestBody RotationConfigRequestDTO request) {
        return ResponseEntity.ok(sessionService.updateConfig(id, request));
    }

    @PostMapping("/{id}/mark-all-bench")
    public ResponseEntity<RotationStateResponseDTO> markAllBench(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.markAllBench(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<RotationStateResponseDTO> start(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.start(id));
    }

    @PostMapping("/{id}/fill-empty-courts")
    public ResponseEntity<RotationStateResponseDTO> fillEmptyCourts(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.fillEmptyCourts(id));
    }

    @PatchMapping("/{id}/screen")
    public ResponseEntity<RotationStateResponseDTO> updateScreen(@PathVariable UUID id, @RequestBody RotationScreenUpdateDTO request) {
        return ResponseEntity.ok(sessionService.updateScreen(id, request));
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<RotationStateResponseDTO> reset(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.reset(id));
    }
}
