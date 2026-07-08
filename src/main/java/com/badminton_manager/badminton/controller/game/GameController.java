package com.badminton_manager.badminton.controller.game;

import com.badminton_manager.badminton.dto.game.GameRequestDTO;
import com.badminton_manager.badminton.dto.game.GameResponseDTO;
import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.service.game.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Games")
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/court/{courtId}")
    public ResponseEntity<List<GameResponseDTO>> getByCourt(@PathVariable UUID courtId) {
        return ResponseEntity.ok(gameService.getByCourt(courtId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(gameService.getById(id));
    }

    @PostMapping
    public ResponseEntity<GameResponseDTO> create(@RequestBody GameRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDTO> update(@PathVariable UUID id, @RequestBody GameRequestDTO request) {
        return ResponseEntity.ok(gameService.update(id, request));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<GameResponseDTO> finishGame(@PathVariable UUID id, @RequestParam CourtWinner winner) {
        return ResponseEntity.ok(gameService.finishGame(id, winner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        gameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
