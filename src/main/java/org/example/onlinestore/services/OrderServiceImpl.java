package org.example.onlinestore.services;

import lombok.extern.slf4j.Slf4j;
import org.example.onlinestore.dtos.*;
import org.example.onlinestore.exceptions.*;
import org.example.onlinestore.models.*;
import org.example.onlinestore.repositories.OrderRepository;
import org.example.onlinestore.repositories.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        return convertToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest orderRequest) {
        log.info("Creating a new order for customer: {}", orderRequest.getCustomerEmail());
        validateUniqueProductsInOrder(orderRequest);

        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setCustomerEmail(orderRequest.getCustomerEmail());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> {
                        log.error("Product not found with id: {}", itemRequest.getProductId());
                        return new ProductNotFoundException("Product not found with id: " + itemRequest.getProductId());
                    });

            if (product.getStock() == 0 || !product.isActive()) {
                throw new InsufficientStockException("Product " + product.getName() + " is out of stock or not available.");
            }

            if (product.getStock() < itemRequest.getQuantity()) {
                log.warn("Insufficient stock for product: {}. Requested: {}, Available: {}", product.getName(), itemRequest.getQuantity(), product.getStock());
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }

        order.setTotalAmount(totalAmount);

        updateStockForOrder(order);

        Order savedOrder = orderRepository.save(order);
        log.info("Order with id: {} created successfully.", savedOrder.getId());
        return convertToResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException("Order can only be modified if status is PENDING.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        log.info("Attempting to cancel order with id: {}", id);
        Order order = findOrderById(id);

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusException("Cannot cancel an order that has been shipped or delivered.");
        }

        if(order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order with id: {} has been cancelled. Stock restored.", id);
    }

    private void updateStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantity();
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    private void validateUniqueProductsInOrder(CreateOrderRequest request) {
        Set<Long> productIds = new HashSet<>();
        for (OrderItemRequest item : request.getOrderItems()) {
            if (!productIds.add(item.getProductId())) {
                throw new InvalidOrderStatusException("Duplicate product found in order. Each product can only appear once.");
            }
        }
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        BeanUtils.copyProperties(order, response, "orderItems");

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            BeanUtils.copyProperties(item, itemResponse);
            if (item.getProduct() != null) {
                itemResponse.setProductId(item.getProduct().getId());
                itemResponse.setProductName(item.getProduct().getName());
            }
            return itemResponse;
        }).collect(Collectors.toList());

        response.setOrderItems(itemResponses);
        return response;
    }
}