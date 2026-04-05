package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.*;
import com.balu.ecommerce.entity.RefreshToken;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.exception.DuplicateEmailException;
import com.balu.ecommerce.exception.InvalidCredentialsException;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.UserRepository;
import com.balu.ecommerce.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // REGISTER
    public UserResponseDTO register(RegisterRequestDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(User.Role.CUSTOMER); //default

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    // LOGIN (Basic — JWT will replace this in Phase 2)
    // UPDATE login method — change return type to LoginResponseDTO
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + dto.getEmail()));

        // Compare raw password with encrypted password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Password");
        }

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                "Bearer",
                refreshToken.getToken()
        );
    }

    public void changePassword(String email, ChangePasswordRequestDTO dto) {

        // Step 1: Find user by email (extracted from JWT)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Step 2: Verify current password is correct
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current Password is incorrect");
        }

        // Step 3: Check new password and confirm password match
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Step 4: Check new password is different from current
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Step 5: Encode and save new password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Step 6: Invalidate all refresh tokens — force re-login on all devices
        refreshTokenService.deleteAllUserTokens(user.getId());
    }

    // MAPPER
    private UserResponseDTO mapToDto(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
