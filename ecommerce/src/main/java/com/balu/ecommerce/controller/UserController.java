package com.balu.ecommerce.controller;

import com.balu.ecommerce.dto.LoginRequestDTO;
import com.balu.ecommerce.dto.RegisterRequestDTO;
import com.balu.ecommerce.dto.UserResponseDTO;
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

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO userResponseDTO = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        UserResponseDTO userResponseDTO = userService.login(dto);
        return ResponseEntity.ok(userResponseDTO);
    }
}
