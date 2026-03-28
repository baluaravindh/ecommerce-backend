package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.OrderItemRequestDTO;
import com.balu.ecommerce.dto.OrderItemResponseDTO;
import com.balu.ecommerce.dto.OrderRequestDTO;
import com.balu.ecommerce.dto.OrderResponseDTO;
import com.balu.ecommerce.entity.Order;
import com.balu.ecommerce.entity.OrderItem;
import com.balu.ecommerce.entity.Product;
import com.balu.ecommerce.entity.User;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.OrderRepository;
import com.balu.ecommerce.repository.ProductRepository;
import com.balu.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // PLACE ORDER
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO dto) {
        // 1. Validate user exists
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        // 2. Create the order
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(dto.getShippingAddress());
//        order.setTotalAmount(BigDecimal.ZERO); // Temporary value
//        Order savedOrder = orderRepository.save(order); // ← Save first, get ID

        // 3. Process each item
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            // Validate product exists
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemDto.getProductId()));

            // Check stock availability
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: "
                        + product.getName() + ". Available: "
                        + product.getStockQuantity());
            }

            // Create order item — snapshot the price NOW
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice()); // snapshot price

            order.getOrderItems().add(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            // Add to total
            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToDto(savedOrder);
    }

    // GET ORDER BY ID
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToDto(order);
    }

    // GET ALL ORDERS OF A USER
    public List<OrderResponseDTO> getAllOrdersByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // UPDATE ORDER STATUS
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        Order updated = orderRepository.save(order);
        return mapToDto(updated);
    }

    // MAPPER
    private OrderResponseDTO mapToDto(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems()
                .stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .collect(Collectors.toList());
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                itemDTOs
        );
    }
}
