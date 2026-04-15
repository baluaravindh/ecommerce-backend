package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.OrderItemRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {

        // User
        user = new User();
        user.setId(1L);
        user.setFullName("Balu Aravindh G");
        user.setEmail("balu@gmail.com");

        // Product
        product = new Product();
        product.setId(1L);
        product.setName("Google Pixel 10 Pro");
        product.setPrice(new BigDecimal("109999.00"));
        product.setStockQuantity(25);

        // OrderItem (for use in pre-built order mocks)
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);
        orderItem.setPriceAtPurchase(new BigDecimal("99999.00"));

        // Order
        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("299997.00"));
        order.setShippingAddress("Puttur, Andhra Pradesh");
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderItems(List.of(orderItem));
        orderItem.setOrder(order);
    }

    // ==================== CREATE ORDER TESTS ====================

    @Test
    @DisplayName("Should create Order successfully")
    void createOrder_successfully() {

        // ARRANGE
        OrderItemRequestDTO itemDto = new OrderItemRequestDTO();
        itemDto.setProductId(1L);
        itemDto.setQuantity(3);

        OrderRequestDTO orderDto = new OrderRequestDTO();
        orderDto.setUserId(1L);
        orderDto.setShippingAddress("Puttur, Andhra Pradesh");
        orderDto.setItems(List.of(itemDto));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // ACT
        OrderResponseDTO result = orderService.createOrder(orderDto);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(orderDto.getUserId());
        assertThat(result.getUserName()).isEqualTo("Balu Aravindh G");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("299997.00"));
        assertThat(result.getShippingAddress()).isEqualTo("Puttur, Andhra Pradesh");

        // VERIFY
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw user not found exception")
    void createOrder_UserNotFound() {

        // ARRANGE
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setUserId(99L);
        dto.setItems(Collections.emptyList());

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ==================== CREATE ORDER PRODUCT NOT FOUND TESTS ====================

    @Test
    @DisplayName("Should throw product not found exception")
    void createProduct_ProductNotFound() {

        // ARRANGE
        OrderItemRequestDTO itemDto = new OrderItemRequestDTO();
        itemDto.setProductId(99L);
        itemDto.setQuantity(3);

        OrderRequestDTO orderDto = new OrderRequestDTO();
        orderDto.setUserId(1L);
        orderDto.setItems(List.of(itemDto));
        orderDto.setShippingAddress("Puttur, Andhra Pradesh");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.createOrder(orderDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    // ==================== CREATE ORDER INSUFFICIENT STOCK TESTS ====================

    @Test
    @DisplayName("Should throw Insufficient stock exception")
    void createOrder_InsufficientStock() {

        // ARRANGE
        OrderItemRequestDTO itemDto = new OrderItemRequestDTO();
        itemDto.setProductId(1L);
        itemDto.setQuantity(30);

        OrderRequestDTO orderDto = new OrderRequestDTO();
        orderDto.setUserId(1L);
        orderDto.setItems(List.of(itemDto));
        orderDto.setShippingAddress("Puttur, Andhra Pradesh");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.createOrder(orderDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
    }

    // ==================== GET ORDER BY ID TESTS ====================

    @Test
    @DisplayName("Should give the order by id successfully")
    void getOrderById_Successfully() {

        // ARRANGE
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // ACT
        OrderResponseDTO result = orderService.getOrderById(1L);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PENDING");


        // VERIFY
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should not give the order by id successfully")
    void getOrderById_OrderNotFound() {

        // ARRANGE
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 99");
    }

    // ==================== GET ORDER BY USER TESTS ====================

    @Test
    @DisplayName("Should give the order by user successfully")
    void getUserByUser_Successfully() {

        // ARRANGE
        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        // ACT
        List<OrderResponseDTO> result = orderService.getAllOrdersByUser(1L);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);

        // VERIFY
        verify(orderRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Should not give the order by user successfully")
    void getUserByUser_UserNotFound() {

        // ARRANGE
        when(userRepository.existsById(99L)).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.getAllOrdersByUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ==================== UPDATE ORDER STATUS TESTS ====================

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrder_Successfully() {

        // ARRANGE
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // ACT
        OrderResponseDTO result = orderService.updateOrderStatus(1L, "CONFIRMED");

        // ASSERT
        assertThat(result).isNotNull();

        // VERIFY
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should not update order status successfully")
    void updateOrder_OrderNotFound() {

        // ARRANGE
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, "CONFIRMED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 99");
    }
}
