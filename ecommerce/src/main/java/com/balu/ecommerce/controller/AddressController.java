package com.balu.ecommerce.controller;

import com.balu.ecommerce.dto.AddressRequestDTO;
import com.balu.ecommerce.dto.AddressResponseDTO;
import com.balu.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // POST /api/users/1/addresses
    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(@PathVariable Long userId,
                                                         @Valid @RequestBody AddressRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(userId, dto));
    }

    // GET /api/users/1/addresses
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    // DELETE /api/users/1/addresses/2
    @DeleteMapping("{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
