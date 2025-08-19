package org.example.onlinestore.services;

import org.example.onlinestore.dtos.CreateOrderRequest;
import org.example.onlinestore.dtos.OrderResponse;
import org.example.onlinestore.models.OrderStatus;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getAllOrders();
    OrderResponse getOrderById(Long id);
    OrderResponse createOrder(CreateOrderRequest orderRequest);
    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);
    void cancelOrder(Long id);
    List<OrderResponse> getOrdersByCustomerEmail(String email);
}