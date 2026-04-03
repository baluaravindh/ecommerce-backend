package com.balu.ecommerce.controller;

import com.balu.ecommerce.dto.*;
import com.balu.ecommerce.entity.RefreshToken;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.security.JwtUtil;
import com.balu.ecommerce.service.RefreshTokenService;
import com.balu.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO userResponseDTO = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO userResponseDTO = userService.login(dto);
        return ResponseEntity.ok(userResponseDTO);
    }

    // POST /api/users/refresh
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {

        // Validate refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(dto.getRefreshToken());

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        // Generate new refresh token (rotate for security)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginResponseDTO response = new LoginResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                "Bearer",
                newRefreshToken.getToken()
        );
        return ResponseEntity.ok(response);
    }

    // POST /api/users/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        refreshTokenService.revokeRefreshToken(dto.getRefreshToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
