package com.balu.ecommerce.controller;

import com.balu.ecommerce.dto.OrderRequestDTO;
import com.balu.ecommerce.dto.OrderResponseDTO;
import com.balu.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(dto));
    }

    // GET /api/orders/1
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // GET /api/orders/user/1
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserDTOs(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getAllOrdersByUser(userId));
    }

    // PATCH /api/orders/1/status?status=CONFIRMED
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
