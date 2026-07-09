package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationPlayerRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerStatusUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerUpdateDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.service.rotation.RotationPlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Rotation Players")
@RestController
@RequestMapping("/api/rotation-players")
public class RotationPlayerController {

    private final RotationPlayerService playerService;

    public RotationPlayerController(RotationPlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<RotationStateResponseDTO> create(@RequestBody RotationPlayerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RotationStateResponseDTO> update(@PathVariable UUID id, @RequestBody RotationPlayerUpdateDTO request) {
        return ResponseEntity.ok(playerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RotationStateResponseDTO> updateStatus(@PathVariable UUID id, @RequestBody RotationPlayerStatusUpdateDTO request) {
        return ResponseEntity.ok(playerService.updateStatus(id, request));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<RotationStateResponseDTO> leave(@PathVariable UUID id) {
        return ResponseEntity.ok(playerService.leave(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
