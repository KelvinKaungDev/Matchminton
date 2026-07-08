package com.badminton_manager.badminton.controller.player;

import com.badminton_manager.badminton.dto.player.PlayerRequestDTO;
import com.badminton_manager.badminton.dto.player.PlayerResponseDTO;
import com.badminton_manager.badminton.service.player.PlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Players")
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/court/{courtId}")
    public ResponseEntity<List<PlayerResponseDTO>> getByCourt(@PathVariable UUID courtId) {
        return ResponseEntity.ok(playerService.getByCourt(courtId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(playerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlayerResponseDTO> create(@RequestBody PlayerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> update(@PathVariable UUID id, @RequestBody PlayerRequestDTO request) {
        return ResponseEntity.ok(playerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
