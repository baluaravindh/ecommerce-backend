package com.balu.ecommerce.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String fullName;
    private String email;
    private String password;
    private String phone;
}
