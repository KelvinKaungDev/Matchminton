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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration rejected, email already exists: {}", request.getEmail());
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(Provider.local);
        user.setTier(Tier.free);
        user = userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getId(), user.getName(), user.getEmail());
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed, unknown email: {}", request.getEmail());
                    return new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
                });

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed, invalid password for: {}", request.getEmail());
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponseDTO(token, user.getId(), user.getName(), user.getEmail());
    }
}
