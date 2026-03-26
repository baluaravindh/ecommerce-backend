package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.LoginRequestDTO;
import com.balu.ecommerce.dto.RegisterRequestDTO;
import com.balu.ecommerce.dto.UserResponseDTO;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.exception.DuplicateEmailException;
import com.balu.ecommerce.exception.InvalidCredentialsException;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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
    public UserResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + dto.getEmail()));
        // Compare raw password with encrypted password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Password");
        }
        return mapToDto(user);
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
