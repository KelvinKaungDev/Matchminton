package com.badminton_manager.badminton.service.group;

import com.badminton_manager.badminton.dto.group.GroupRequestDTO;
import com.badminton_manager.badminton.dto.group.GroupResponseDTO;
import com.badminton_manager.badminton.enums.SkillLevel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    List<GroupResponseDTO> getAll();
    List<GroupResponseDTO> getActive();
    List<GroupResponseDTO> getByOrganizer(UUID organizerId);
    GroupResponseDTO getById(UUID id);
    List<GroupResponseDTO> searchByName(String namePrefix);
    List<GroupResponseDTO> filterBySkillLevel(SkillLevel skillLevel);
    GroupResponseDTO create(GroupRequestDTO request);
    GroupResponseDTO update(UUID id, GroupRequestDTO request);
    GroupResponseDTO uploadPhoto(UUID id, MultipartFile file);
    void delete(UUID id);
}
