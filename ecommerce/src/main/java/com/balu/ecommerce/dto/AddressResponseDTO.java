package com.balu.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDTO {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String addressType;
    private boolean isDefault;
    private Long userId;
}
