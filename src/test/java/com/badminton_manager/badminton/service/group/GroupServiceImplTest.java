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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private GroupServiceImpl groupService;

    private User organizer;
    private Group group;
    private UUID organizerId;
    private UUID groupId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        groupId = UUID.randomUUID();

        organizer = new User();
        organizer.setId(organizerId);
        organizer.setName("Kelvin");

        group = new Group();
        group.setId(groupId);
        group.setOrganizer(organizer);
        group.setName("SundayFunDay");
        group.setSkillLevel(SkillLevel.B);
        group.setActive(true);
        group.setCreatedAt(Instant.now());
    }

    @Test
    void getAll_returnsAllGroups() {
        when(groupRepository.findAll()).thenReturn(List.of(group));

        List<GroupResponseDTO> result = groupService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("SundayFunDay");
    }

    @Test
    void getActive_returnsOnlyActiveGroups() {
        when(groupRepository.findByIsActiveTrue()).thenReturn(List.of(group));

        List<GroupResponseDTO> result = groupService.getActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void getByOrganizer_returnsGroupsForOrganizer() {
        when(groupRepository.findByOrganizerId(organizerId)).thenReturn(List.of(group));

        List<GroupResponseDTO> result = groupService.getByOrganizer(organizerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganizerName()).isEqualTo("Kelvin");
    }

    @Test
    void getById_existingId_returnsGroup() {
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        GroupResponseDTO result = groupService.getById(groupId);

        assertThat(result.getId()).isEqualTo(groupId);
        assertThat(result.getName()).isEqualTo("SundayFunDay");
        assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.B);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void searchByName_matchingPrefix_returnsOnlyMatchingGroupsSorted() {
        Group sundaySmash = new Group();
        sundaySmash.setId(UUID.randomUUID());
        sundaySmash.setOrganizer(organizer);
        sundaySmash.setName("Sunday Smash");
        sundaySmash.setSkillLevel(SkillLevel.A);
        sundaySmash.setActive(true);
        sundaySmash.setCreatedAt(Instant.now());

        Group mondayMixers = new Group();
        mondayMixers.setId(UUID.randomUUID());
        mondayMixers.setOrganizer(organizer);
        mondayMixers.setName("Monday Mixers");
        mondayMixers.setSkillLevel(SkillLevel.C);
        mondayMixers.setActive(true);
        mondayMixers.setCreatedAt(Instant.now());

        when(groupRepository.findAll()).thenReturn(List.of(mondayMixers, group, sundaySmash));

        List<GroupResponseDTO> result = groupService.searchByName("sun");

        assertThat(result).extracting(GroupResponseDTO::getName)
                .containsExactly("Sunday Smash", "SundayFunDay");
    }

    @Test
    void searchByName_noMatch_returnsEmptyList() {
        when(groupRepository.findAll()).thenReturn(List.of(group));

        List<GroupResponseDTO> result = groupService.searchByName("zzz");

        assertThat(result).isEmpty();
    }

    @Test
    void filterBySkillLevel_matchingLevel_returnsOnlyThatLevel() {
        Group advancedGroup = new Group();
        advancedGroup.setId(UUID.randomUUID());
        advancedGroup.setOrganizer(organizer);
        advancedGroup.setName("Advanced Group");
        advancedGroup.setSkillLevel(SkillLevel.A);
        advancedGroup.setActive(true);
        advancedGroup.setCreatedAt(Instant.now());

        when(groupRepository.findAll()).thenReturn(List.of(advancedGroup, group));

        List<GroupResponseDTO> result = groupService.filterBySkillLevel(SkillLevel.B);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkillLevel()).isEqualTo(SkillLevel.B);
    }

    @Test
    void filterBySkillLevel_noMatch_returnsEmptyList() {
        when(groupRepository.findAll()).thenReturn(List.of(group));

        List<GroupResponseDTO> result = groupService.filterBySkillLevel(SkillLevel.S);

        assertThat(result).isEmpty();
    }

    @Test
    void create_validRequest_savesAndReturnsGroup() {
        GroupRequestDTO request = new GroupRequestDTO();
        request.setOrganizerId(organizerId);
        request.setName("SundayFunDay");
        request.setSkillLevel(SkillLevel.B);
        request.setDescription("Sunday session");

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        GroupResponseDTO result = groupService.create(request);

        assertThat(result.getName()).isEqualTo("SundayFunDay");
        assertThat(result.getOrganizerName()).isEqualTo("Kelvin");
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void create_organizerNotFound_throwsResourceNotFoundException() {
        UUID unknownOrganizerId = UUID.randomUUID();
        GroupRequestDTO request = new GroupRequestDTO();
        request.setOrganizerId(unknownOrganizerId);

        when(userRepository.findById(unknownOrganizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownOrganizerId.toString());

        verify(groupRepository, never()).save(any());
    }

    @Test
    void update_existingId_updatesGroup() {
        GroupRequestDTO request = new GroupRequestDTO();
        request.setName("SundayFunDay Updated");
        request.setSkillLevel(SkillLevel.A);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        GroupResponseDTO result = groupService.update(groupId, request);

        assertThat(result).isNotNull();
        verify(groupRepository).save(group);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.update(unknownId, new GroupRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void uploadPhoto_existingGroupWithNoPreviousPhoto_setsPhotoUrlAndSkipsDelete() {
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "fake-bytes".getBytes());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(fileStorageService.store(file, "groups")).thenReturn("/uploads/groups/new-photo.png");
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        GroupResponseDTO result = groupService.uploadPhoto(groupId, file);

        assertThat(result.getPhotoUrl()).isEqualTo("/uploads/groups/new-photo.png");
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    void uploadPhoto_existingGroupWithPreviousPhoto_replacesAndDeletesOldPhoto() {
        group.setPhotoUrl("/uploads/groups/old-photo.png");
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "fake-bytes".getBytes());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(fileStorageService.store(file, "groups")).thenReturn("/uploads/groups/new-photo.png");
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        GroupResponseDTO result = groupService.uploadPhoto(groupId, file);

        assertThat(result.getPhotoUrl()).isEqualTo("/uploads/groups/new-photo.png");
        verify(fileStorageService).delete("/uploads/groups/old-photo.png");
    }

    @Test
    void uploadPhoto_groupNotFound_throwsResourceNotFoundExceptionWithoutStoringFile() {
        UUID unknownId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "fake-bytes".getBytes());

        when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.uploadPhoto(unknownId, file))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(fileStorageService, never()).store(any(), any());
    }

    @Test
    void delete_existingId_deletesGroup() {
        when(groupRepository.existsById(groupId)).thenReturn(true);

        groupService.delete(groupId);

        verify(groupRepository).deleteById(groupId);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(groupRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> groupService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(groupRepository, never()).deleteById(any());
    }
}
