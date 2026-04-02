package com.balu.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String token; // ← JWT token
    private String tokenType; // ← always "Bearer"
}
