package com.badminton_manager.badminton.controller.session;

import com.badminton_manager.badminton.dto.session.CompetitionSessionRequestDTO;
import com.badminton_manager.badminton.dto.session.CompetitionSessionResponseDTO;
import com.badminton_manager.badminton.service.session.CompetitionSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Competition Sessions")
@RestController
@RequestMapping("/api/competition-sessions")
public class CompetitionSessionController {

    private final CompetitionSessionService sessionService;

    public CompetitionSessionController(CompetitionSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<CompetitionSessionResponseDTO>> getAll() {
        return ResponseEntity.ok(sessionService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CompetitionSessionResponseDTO>> getActive() {
        return ResponseEntity.ok(sessionService.getActive());
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<CompetitionSessionResponseDTO>> getByOrganizer(@PathVariable UUID organizerId) {
        return ResponseEntity.ok(sessionService.getByOrganizer(organizerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionSessionResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CompetitionSessionResponseDTO> create(@RequestBody CompetitionSessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionSessionResponseDTO> update(@PathVariable UUID id, @RequestBody CompetitionSessionRequestDTO request) {
        return ResponseEntity.ok(sessionService.update(id, request));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<CompetitionSessionResponseDTO> finish(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.finish(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
