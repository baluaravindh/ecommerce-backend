package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.AddressRequestDTO;
import com.balu.ecommerce.dto.AddressResponseDTO;
import com.balu.ecommerce.entity.Address;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.AddressRepository;
import com.balu.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // ADD address to a user
    public AddressResponseDTO addAddress(Long userId, AddressRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        address.setAddressType(dto.getAddressType());
        address.setDefault(dto.isDefault());
        address.setUser(user); // Link address to user

        Address saved = addressRepository.save(address);
        return mapToDto(saved);
    }

    // GET all addresses of a user
    public List<AddressResponseDTO> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // DELETE an address
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        addressRepository.delete(address);
    }

    // MAPPER
    private AddressResponseDTO mapToDto(Address address) {
        return new AddressResponseDTO(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getAddressType(),
                address.isDefault(),
                address.getUser().getId()
        );
    }
}
