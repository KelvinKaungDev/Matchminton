package com.badminton_manager.badminton.config;

import com.badminton_manager.badminton.filter.RequestLoggingFilter;
import com.badminton_manager.badminton.security.CustomUserDetailsService;
import com.badminton_manager.badminton.security.JwtAuthenticationFilter;
import com.badminton_manager.badminton.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        RequestLoggingFilter requestLoggingFilter = new RequestLoggingFilter();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/uploads/**"
                        ).permitAll()
                        // Judges and scoreboard viewers use a court code with no login (never had
                        // one, even under the old Firestore rules). Viewing and live scoring are
                        // public; creating sessions/courts/rosters still requires the organizer's JWT.
                        .requestMatchers(HttpMethod.GET, "/api/competition-sessions/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/competition-courts/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/competition-courts/*/finish").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/players/court/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/court/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/games").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/games/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/games/*/finish").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(String.format(
                            "{\"status\":401,\"message\":\"Unauthorized: valid token required\",\"timestamp\":\"%s\"}",
                            LocalDateTime.now()
                    ));
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestLoggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
