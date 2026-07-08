package com.badminton_manager.badminton.service.user;

import com.badminton_manager.badminton.dto.user.UserRequestDTO;
import com.badminton_manager.badminton.dto.user.UserResponseDTO;
import com.badminton_manager.badminton.enums.Provider;
import com.badminton_manager.badminton.enums.Tier;
import com.badminton_manager.badminton.exception.ResourceNotFoundException;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Kelvin");
        user.setEmail("kelvin@example.com");
        user.setAvatar("https://avatar.url");
        user.setProvider(Provider.google);
        user.setTier(Tier.free);
        user.setCreatedAt(Instant.now());
    }

    @Test
    void getAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDTO> result = userService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Kelvin");
        assertThat(result.get(0).getEmail()).isEqualTo("kelvin@example.com");
    }

    @Test
    void getById_existingId_returnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Kelvin");
        assertThat(result.getProvider()).isEqualTo(Provider.google);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void create_savesAndReturnsUser() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Kelvin");
        request.setEmail("kelvin@example.com");
        request.setAvatar("https://avatar.url");
        request.setProvider(Provider.google);

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.create(request);

        assertThat(result.getName()).isEqualTo("Kelvin");
        assertThat(result.getTier()).isEqualTo(Tier.free);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_withTier_savesWithGivenTier() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Pro Player");
        request.setEmail("pro@example.com");
        request.setProvider(Provider.line);
        request.setTier(Tier.pro);

        User proUser = new User();
        proUser.setId(UUID.randomUUID());
        proUser.setName("Pro Player");
        proUser.setEmail("pro@example.com");
        proUser.setProvider(Provider.line);
        proUser.setTier(Tier.pro);

        when(userRepository.save(any(User.class))).thenReturn(proUser);

        UserResponseDTO result = userService.create(request);

        assertThat(result.getTier()).isEqualTo(Tier.pro);
    }

    @Test
    void update_existingId_updatesAndReturnsUser() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Kelvin Updated");
        request.setEmail("updated@example.com");
        request.setProvider(Provider.google);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.update(userId, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(unknownId, new UserRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesUser() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.existsById(unknownId)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
