package com.badminton_manager.badminton.controller.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationCourtSwapRequestDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.service.rotation.RotationCourtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Rotation Courts")
@RestController
@RequestMapping("/api/rotation-courts")
public class RotationCourtController {

    private final RotationCourtService courtService;

    public RotationCourtController(RotationCourtService courtService) {
        this.courtService = courtService;
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RotationStateResponseDTO> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(courtService.complete(id));
    }

    @PostMapping("/{id}/refill")
    public ResponseEntity<RotationStateResponseDTO> refill(@PathVariable UUID id) {
        return ResponseEntity.ok(courtService.refill(id));
    }

    @PostMapping("/{id}/swap")
    public ResponseEntity<RotationStateResponseDTO> swap(@PathVariable UUID id, @RequestBody RotationCourtSwapRequestDTO request) {
        return ResponseEntity.ok(courtService.swap(id, request));
    }
}
