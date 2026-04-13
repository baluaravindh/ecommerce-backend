package com.balu.ecommerce.service;

import com.balu.ecommerce.dto.ProductDto;
import com.balu.ecommerce.entity.Product;
import com.balu.ecommerce.exception.ResourceNotFoundException;
import com.balu.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
        // Runs before EACH test method — fresh data every time
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setDescription("Apple iPhone 15 128GB");
        product.setPrice(new BigDecimal("79990.00"));
        product.setStockQuantity(50);
        product.setCategory("Mobiles");
        product.setImageUrl("https://example.com/iphone15.jpg");

        productDto = new ProductDto();
        productDto.setName("iPhone 15");
        productDto.setDescription("Apple iPhone 15 128GB");
        productDto.setPrice(new BigDecimal("79990.00"));
        productDto.setStockQuantity(50);
        productDto.setCategory("Mobiles");
        productDto.setImageUrl("https://example.com/iphone15.jpg");
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_success() {
        // ARRANGE
        // Tell the fake repo: when save() is called with any Product,
        // return our test product

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // ACT
        ProductDto result = productService.createProduct(productDto);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("79990.00"));
        assertThat(result.getCategory()).isEqualTo("Mobiles");

        // Verify save was called exactly once
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    @DisplayName("Should return all products")
    void getAllProducts_success() {
        // ARRANGE
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung Galaxy S24");
        product2.setPrice(new BigDecimal("65000.00"));
        product2.setStockQuantity(30);
        product2.setCategory("Mobiles");

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        List<ProductDto> result = productService.getAllProducts();

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("iPhone 15");
        assertThat(result.get(1).getName()).isEqualTo("Samsung Galaxy S24");

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void getAllProducts_EmptyList() {
        when(productRepository.findAll()).thenReturn(Arrays.asList());
        List<ProductDto> result = productService.getAllProducts();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
//        verify(productRepository, times(1)).findAll();
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("Should return product when valid id provided")
    void getProductById_Success() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDto result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getId()).isEqualTo(1L);

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product id not found.")
    void getProductById_NotFound() {

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository, times(1)).findById(99L);
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_Success() {
        ProductDto updatedDto = new ProductDto();
        updatedDto.setName("iPhone 15 Pro");
        updatedDto.setDescription("Updated Description");
        updatedDto.setPrice(new BigDecimal("99990.00"));
        updatedDto.setStockQuantity(25);
        updatedDto.setCategory("Mobiles");
        updatedDto.setImageUrl("https://example.com/iphone15pro.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.updateProduct(1L, updatedDto);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, productDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProductById(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");

        verify(productRepository, never()).delete(any(Product.class));
    }
}
