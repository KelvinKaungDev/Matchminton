package com.badminton_manager.badminton.service.group;

import com.badminton_manager.badminton.dto.group.GroupRequestDTO;
import com.badminton_manager.badminton.dto.group.GroupResponseDTO;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.Group;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.GroupRepository;
import com.badminton_manager.badminton.repository.UserRepository;
import com.badminton_manager.badminton.service.storage.FileStorageService;
import com.badminton_manager.badminton.util.BinarySearchUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public GroupServiceImpl(GroupRepository groupRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public List<GroupResponseDTO> getAll() {
        return groupRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<GroupResponseDTO> getActive() {
        return groupRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<GroupResponseDTO> getByOrganizer(UUID organizerId) {
        return groupRepository.findByOrganizerId(organizerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GroupResponseDTO getById(UUID id) {
        return groupRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    @Override
    public List<GroupResponseDTO> searchByName(String namePrefix) {
        String prefix = namePrefix.toLowerCase();
        List<Group> sorted = new ArrayList<>(groupRepository.findAll());
        sorted.sort(Comparator.comparing(g -> g.getName().toLowerCase()));

        int from = BinarySearchUtil.lowerBound(sorted, g -> comparePrefix(g.getName(), prefix));
        int to = BinarySearchUtil.upperBound(sorted, g -> comparePrefix(g.getName(), prefix));

        return sorted.subList(from, to).stream().map(this::toResponse).toList();
    }

    @Override
    public List<GroupResponseDTO> filterBySkillLevel(SkillLevel skillLevel) {
        List<Group> sorted = new ArrayList<>(groupRepository.findAll());
        sorted.sort(Comparator.comparing(Group::getSkillLevel));

        int from = BinarySearchUtil.lowerBound(sorted, g -> g.getSkillLevel().compareTo(skillLevel));
        int to = BinarySearchUtil.upperBound(sorted, g -> g.getSkillLevel().compareTo(skillLevel));

        return sorted.subList(from, to).stream().map(this::toResponse).toList();
    }

    private int comparePrefix(String candidateName, String prefixLower) {
        String candidate = candidateName.toLowerCase();
        String truncated = candidate.length() > prefixLower.length()
                ? candidate.substring(0, prefixLower.length())
                : candidate;
        return truncated.compareTo(prefixLower);
    }

    @Override
    public GroupResponseDTO create(GroupRequestDTO request) {
        User organizer = userRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOrganizerId()));

        Group group = new Group();
        group.setOrganizer(organizer);
        group.setName(request.getName());
        group.setSkillLevel(request.getSkillLevel());
        group.setPhotoUrl(request.getPhotoUrl());
        group.setLineId(request.getLineId());
        group.setDescription(request.getDescription());
        if (request.getIsActive() != null) group.setActive(request.getIsActive());

        return toResponse(groupRepository.save(group));
    }

    @Override
    public GroupResponseDTO update(UUID id, GroupRequestDTO request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));

        if (request.getOrganizerId() != null) {
            User organizer = userRepository.findById(request.getOrganizerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOrganizerId()));
            group.setOrganizer(organizer);
        }

        group.setName(request.getName());
        group.setSkillLevel(request.getSkillLevel());
        group.setPhotoUrl(request.getPhotoUrl());
        group.setLineId(request.getLineId());
        group.setDescription(request.getDescription());
        if (request.getIsActive() != null) group.setActive(request.getIsActive());

        return toResponse(groupRepository.save(group));
    }

    @Override
    public GroupResponseDTO uploadPhoto(UUID id, MultipartFile file) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));

        String oldPhotoUrl = group.getPhotoUrl();
        String newPhotoUrl = fileStorageService.store(file, "groups");
        group.setPhotoUrl(newPhotoUrl);
        Group saved = groupRepository.save(group);

        if (oldPhotoUrl != null) {
            fileStorageService.delete(oldPhotoUrl);
        }

        return toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Group not found with id: " + id);
        }
        groupRepository.deleteById(id);
    }

    private GroupResponseDTO toResponse(Group group) {
        GroupResponseDTO dto = new GroupResponseDTO();
        dto.setId(group.getId());
        dto.setOrganizerId(group.getOrganizer().getId());
        dto.setOrganizerName(group.getOrganizer().getName());
        dto.setName(group.getName());
        dto.setSkillLevel(group.getSkillLevel());
        dto.setPhotoUrl(group.getPhotoUrl());
        dto.setLineId(group.getLineId());
        dto.setDescription(group.getDescription());
        dto.setActive(group.isActive());
        dto.setCreatedAt(group.getCreatedAt());
        return dto;
    }
}
