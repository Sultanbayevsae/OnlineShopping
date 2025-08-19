package org.example.onlinestore.services;

import org.example.onlinestore.dtos.CreateOrderRequest;
import org.example.onlinestore.dtos.OrderItemRequest;
import org.example.onlinestore.exceptions.InsufficientStockException;
import org.example.onlinestore.exceptions.ProductNotFoundException;
import org.example.onlinestore.models.Order;
import org.example.onlinestore.models.Product;
import org.example.onlinestore.repositories.OrderRepository;
import org.example.onlinestore.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_ShouldThrowProductNotFoundException() {
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(99L);
        itemRequest.setQuantity(1);
        request.setOrderItems(Collections.singletonList(itemRequest));

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_ShouldThrowInsufficientStockException() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Book");
        product.setStock(5);
        product.setPrice(BigDecimal.TEN);

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(10);
        request.setOrderItems(Collections.singletonList(itemRequest));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_ShouldSucceed() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Book");
        product.setStock(20);
        product.setPrice(new BigDecimal("25.00"));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Test User");
        request.setCustomerEmail("test@example.com");
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        request.setOrderItems(Collections.singletonList(itemRequest));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var orderResponse = orderService.createOrder(request);

        assertNotNull(orderResponse);
        assertEquals(new BigDecimal("50.00"), orderResponse.getTotalAmount());
        assertEquals(18, product.getStock());

        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}