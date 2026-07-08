package com.badminton_manager.badminton.controller.group;

import com.badminton_manager.badminton.dto.group.GroupRequestDTO;
import com.badminton_manager.badminton.dto.group.GroupResponseDTO;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.service.group.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups")
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getAll() {
        return ResponseEntity.ok(groupService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<GroupResponseDTO>> getActive() {
        return ResponseEntity.ok(groupService.getActive());
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<GroupResponseDTO>> getByOrganizer(@PathVariable UUID organizerId) {
        return ResponseEntity.ok(groupService.getByOrganizer(organizerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GroupResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(groupService.searchByName(name));
    }

    @GetMapping("/skill-level/{skillLevel}")
    public ResponseEntity<List<GroupResponseDTO>> filterBySkillLevel(@PathVariable SkillLevel skillLevel) {
        return ResponseEntity.ok(groupService.filterBySkillLevel(skillLevel));
    }

    @PostMapping
    public ResponseEntity<GroupResponseDTO> create(@RequestBody GroupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDTO> update(@PathVariable UUID id, @RequestBody GroupRequestDTO request) {
        return ResponseEntity.ok(groupService.update(id, request));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GroupResponseDTO> uploadPhoto(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(groupService.uploadPhoto(id, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
