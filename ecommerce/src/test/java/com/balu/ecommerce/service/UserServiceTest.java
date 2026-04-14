package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.*;
import com.balu.ecommerce.entity.RefreshToken;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.exception.DuplicateEmailException;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.UserRepository;
import com.balu.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Create a real BCrypt encoded password for testing
        String passwordEncoder = encoder.encode("Balu@123");

        user = new User();
        user.setId(1L);
        user.setFullName("Balu Aravindh G");
        user.setEmail("balu@gmail.com");
        user.setPassword(passwordEncoder);
        user.setPhone("6281435933");
        user.setRole(User.Role.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        registerDTO = new RegisterRequestDTO();
        registerDTO.setFullName("Balu Aravindh G");
        registerDTO.setEmail("balu@gmail.com");
        registerDTO.setPassword("Balu@123");
        registerDTO.setPhone("6281435933");

        loginDTO = new LoginRequestDTO();
        loginDTO.setEmail("balu@gmail.com");
        loginDTO.setPassword("Balu@123");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should register user successfully")
    void register_success() {
        // ARRANGE
        when(userRepository.existsByEmail("balu@gmail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // ACT
        UserResponseDTO result = userService.register(registerDTO);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Balu Aravindh G");
        assertThat(result.getEmail()).isEqualTo("balu@gmail.com");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");

        // VERIFY
        verify(userRepository, times(1)).existsByEmail("balu@gmail.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void register_fail() {
        // ARRANGE
        when(userRepository.existsByEmail("balu@gmail.com")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.register(registerDTO))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("balu@gmail.com");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should login successfully and return JWT token")
    void login_success() {
        // ARRANGE
        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));

        // Tell JwtUtil mock what to return when generateToken is called
        when(jwtUtil.generateToken("balu@gmail.com", "CUSTOMER"))
                .thenReturn("mock.jwt.token");

        // Create a fake RefreshToken to return
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mock-refresh-token-uuid");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        // ACT
        LoginResponseDTO result = userService.login(loginDTO);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("balu@gmail.com");
        assertThat(result.getToken()).isEqualTo("mock.jwt.token");
        assertThat(result.getRefreshToken()).isEqualTo("mock-refresh-token-uuid");
        assertThat(result.getTokenType()).isEqualTo("Bearer");

        // VERIFY
        verify(userRepository, times(1)).findByEmail("balu@gmail.com");
        verify(jwtUtil, times(1)).generateToken("balu@gmail.com", "CUSTOMER");
        verify(refreshTokenService, times(1)).createRefreshToken(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when email not found")
    void login_emailNotFound() {

        // ARRANGE
        when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(Optional.empty());

        LoginRequestDTO wrongDto = new LoginRequestDTO();
        wrongDto.setEmail("wrong@gmail.com");
        wrongDto.setPassword("SomethingWrong");

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.login(wrongDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("wrong@gmail.com");

        // VERIFY
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void login_wrongPassword() {

        // ARRANGE
        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));

        LoginRequestDTO wrongPasswordDto = new LoginRequestDTO();
        wrongPasswordDto.setEmail("balu@gmail.com");
        wrongPasswordDto.setPassword("SomethingWrong");

        // ACT + ASSERT
        assertThatThrownBy(() -> userService.login(wrongPasswordDto))
                .isInstanceOf(Exception.class);

        // VERIFY
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    @DisplayName("Should change password successfully")
    void change_passwordSuccess() {

        // ARRANGE
        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(refreshTokenService).deleteAllUserTokens(1L);

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setCurrentPassword("Balu@123");
        dto.setNewPassword("Newbalu@2026");
        dto.setConfirmNewPassword("Newbalu@2026");

        // ACT
        userService.changePassword("balu@gmail.com", dto);

        // ASSERT
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenService, times(1)).deleteAllUserTokens(1L);
    }

    @Test
    @DisplayName("Should throw exception when current password is wrong")
    void changePassword_WrongCurrentPassword() {

        // ARRANGE
        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setCurrentPassword("WrongPassword");
        dto.setNewPassword("Newbalu@2026");
        dto.setConfirmNewPassword("Newbalu@2026");

        assertThatThrownBy(() -> userService.changePassword("balu@gmail.com", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Current Password is incorrect");

        // Save should never be called if current password is wrong
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new passwords do not match")
    void changePassword_didNotMatch() {

        // ARRANGE
        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setCurrentPassword("Balu@123");
        dto.setNewPassword("Newbalu@2026");
        dto.setConfirmNewPassword("DifferentFromNewPassword");

        assertThatThrownBy(() -> userService.changePassword("balu@gmail.com", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("do not match");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when new password same as current")
    void changePassword_SameAsCurrentPassword() {

        when(userRepository.findByEmail("balu@gmail.com")).thenReturn(Optional.of(user));

        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setCurrentPassword("Balu@123");
        dto.setNewPassword("Balu@123");
        dto.setConfirmNewPassword("Balu@123");

        assertThatThrownBy(() -> userService.changePassword("balu@gmail.com", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("different from current");

        verify(userRepository, never()).save(any(User.class));
    }
}
