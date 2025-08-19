package org.example.onlinestore.services;

import org.example.onlinestore.exceptions.ProductNotFoundException;
import org.example.onlinestore.models.Product;
import org.example.onlinestore.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(new BigDecimal("1000"));
        product.setStock(10);
        product.setCategory("Electronics");
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var productResponse = productService.getProductById(1L);

        assertNotNull(productResponse);
        assertEquals("Test Laptop", productResponse.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(1L);
        });

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void createProduct_ShouldReturnSavedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        var savedProductResponse = productService.createProduct(new Product());

        assertNotNull(savedProductResponse);
        assertEquals(1L, savedProductResponse.getId());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}