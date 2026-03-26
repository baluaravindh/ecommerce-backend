package com.balu.ecommerce.dto;

import com.balu.ecommerce.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
