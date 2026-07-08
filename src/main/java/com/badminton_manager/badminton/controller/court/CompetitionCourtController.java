package com.badminton_manager.badminton.controller.court;

import com.badminton_manager.badminton.dto.court.CompetitionCourtRequestDTO;
import com.badminton_manager.badminton.dto.court.CompetitionCourtResponseDTO;
import com.badminton_manager.badminton.enums.CourtWinner;
import com.badminton_manager.badminton.service.court.CompetitionCourtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Competition Courts")
@RestController
@RequestMapping("/api/competition-courts")
public class CompetitionCourtController {

    private final CompetitionCourtService courtService;

    public CompetitionCourtController(CompetitionCourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<CompetitionCourtResponseDTO>> getBySession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(courtService.getBySession(sessionId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionCourtResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(courtService.getById(id));
    }

    @GetMapping("/code/{courtCode}")
    public ResponseEntity<CompetitionCourtResponseDTO> getByCourtCode(@PathVariable String courtCode) {
        return ResponseEntity.ok(courtService.getByCourtCode(courtCode));
    }

    @PostMapping
    public ResponseEntity<CompetitionCourtResponseDTO> create(@RequestBody CompetitionCourtRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courtService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionCourtResponseDTO> update(@PathVariable UUID id, @RequestBody CompetitionCourtRequestDTO request) {
        return ResponseEntity.ok(courtService.update(id, request));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<CompetitionCourtResponseDTO> finishCourt(@PathVariable UUID id, @RequestParam CourtWinner winner) {
        return ResponseEntity.ok(courtService.finishCourt(id, winner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courtService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
