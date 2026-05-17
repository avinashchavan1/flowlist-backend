package com.flowlist.service;

import com.flowlist.dto.*;
import com.flowlist.entity.PasswordResetToken;
import com.flowlist.entity.User;
import com.flowlist.repository.PasswordResetTokenRepository;
import com.flowlist.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authManager,
                       PasswordResetTokenRepository resetTokenRepository, EmailService emailService,
                       RestTemplate restTemplate) {
        this.userRepository      = userRepository;
        this.passwordEncoder     = passwordEncoder;
        this.jwtService          = jwtService;
        this.authManager         = authManager;
        this.resetTokenRepository = resetTokenRepository;
        this.emailService        = emailService;
        this.restTemplate        = restTemplate;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            req.getEmail().toLowerCase(), req.getPassword()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = userRepository.findByEmail(req.getEmail().toLowerCase()).orElseThrow();
        return buildResponse(user);
    }

    public AuthResponse.UserDto me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // Always returns success — never leak whether an email exists
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            Instant expires = Instant.now().plus(1, ChronoUnit.HOURS);
            resetTokenRepository.save(new PasswordResetToken(token, user.getEmail(), expires));
            emailService.sendPasswordReset(user.getEmail(), token);
        });
    }

    // Cleanup expired reset tokens hourly — moved off forgotPassword path to avoid DoS vector
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanupExpiredResetTokens() {
        resetTokenRepository.deleteExpired(Instant.now());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = resetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset link has expired");
        }
        User user = userRepository.findByEmail(prt.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        resetTokenRepository.save(prt);
    }

    public AuthResponse googleLogin(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;
        Map<String, Object> info;
        try {
            info = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        } catch (HttpClientErrorException e) {
            throw new BadCredentialsException("Invalid Google token");
        }
        if (info == null || !Boolean.TRUE.equals(info.get("email_verified"))) {
            throw new BadCredentialsException("Google account email not verified");
        }
        String email    = ((String) info.get("email")).toLowerCase();
        String name     = (String) info.get("name");
        String googleId = (String) info.get("sub");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // New user via Google — store a random unknown password
            return userRepository.save(User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .build());
        });

        // Update googleId if this is an existing email/password account linking Google for the first time
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInDays(jwtService.getExpirationDays())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build())
                .build();
    }
}
