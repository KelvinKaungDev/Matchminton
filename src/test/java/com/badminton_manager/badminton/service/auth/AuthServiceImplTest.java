package com.badminton_manager.badminton.service.auth;

import com.badminton_manager.badminton.dto.auth.AuthResponseDTO;
import com.badminton_manager.badminton.dto.auth.LoginRequestDTO;
import com.badminton_manager.badminton.dto.auth.RegisterRequestDTO;
import com.badminton_manager.badminton.enums.Provider;
import com.badminton_manager.badminton.enums.Tier;
import com.badminton_manager.badminton.exception.BadRequestException;
import com.badminton_manager.badminton.exception.UnauthorizedException;
import com.badminton_manager.badminton.model.User;
import com.badminton_manager.badminton.repository.UserRepository;
import com.badminton_manager.badminton.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Kelvin");
        user.setEmail("kelvin@example.com");
        user.setPassword("hashed-password");
        user.setProvider(Provider.local);
        user.setTier(Tier.free);
        user.setCreatedAt(Instant.now());
    }

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Kelvin");
        request.setEmail("kelvin@example.com");
        request.setPassword("plain-password");

        when(userRepository.findByEmail("kelvin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken("kelvin@example.com")).thenReturn("jwt-token");

        AuthResponseDTO result = authService.register(request);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("kelvin@example.com");
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsBadRequestException() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Kelvin");
        request.setEmail("kelvin@example.com");
        request.setPassword("plain-password");

        when(userRepository.findByEmail("kelvin@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("kelvin@example.com");
        request.setPassword("plain-password");

        when(userRepository.findByEmail("kelvin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("kelvin@example.com")).thenReturn("jwt-token");

        AuthResponseDTO result = authService.login(request);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("kelvin@example.com");
    }

    @Test
    void login_wrongPassword_throwsUnauthorizedException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("kelvin@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("kelvin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_unknownEmail_throwsUnauthorizedException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("unknown@example.com");
        request.setPassword("plain-password");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }
}
