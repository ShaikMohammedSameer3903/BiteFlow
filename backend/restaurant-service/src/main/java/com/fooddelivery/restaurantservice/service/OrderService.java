package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.common.dto.OrderDTO;
import com.fooddelivery.common.dto.OrderItemDTO;
import com.fooddelivery.restaurantservice.entity.Order;
import com.fooddelivery.restaurantservice.entity.OrderItem;
import com.fooddelivery.restaurantservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order(
            orderDTO.getCustomerId(),
            orderDTO.getRestaurantId(),
            orderDTO.getTotalAmount(),
            orderDTO.getDeliveryAddress()
        );
        
        order.setSpecialInstructions(orderDTO.getSpecialInstructions());
        
        // Add order items
        for (OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem orderItem = new OrderItem(
                itemDTO.getMenuItemId(),
                itemDTO.getItemName(),
                itemDTO.getQuantity(),
                itemDTO.getUnitPrice()
            );
            orderItem.setSpecialRequests(itemDTO.getSpecialRequests());
            order.addItem(orderItem);
        }
        
        // Recalculate total amount
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        return savedOrder.toDTO();
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id).map(Order::toDTO);
    }

    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByStatus(OrderDTO.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getActiveOrdersByRestaurant(Long restaurantId) {
        List<OrderDTO.OrderStatus> activeStatuses = Arrays.asList(
            OrderDTO.OrderStatus.PENDING,
            OrderDTO.OrderStatus.CONFIRMED,
            OrderDTO.OrderStatus.PREPARING,
            OrderDTO.OrderStatus.READY_FOR_PICKUP
        );
        
        return orderRepository.findByRestaurantAndStatuses(restaurantId, activeStatuses).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getRecentOrdersByCustomer(Long customerId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findByCustomerAndDateRange(customerId, startDate).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getRecentOrdersByRestaurant(Long restaurantId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findByRestaurantAndDateRange(restaurantId, startDate).stream()
                .map(Order::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(Long id, OrderDTO.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        order.updateStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return updatedOrder.toDTO();
    }

    public OrderDTO assignDelivery(Long orderId, Long deliveryId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setDeliveryId(deliveryId);
        Order updatedOrder = orderRepository.save(order);
        return updatedOrder.toDTO();
    }

    public OrderDTO assignPayment(Long orderId, Long paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setPaymentId(paymentId);
        Order updatedOrder = orderRepository.save(order);
        return updatedOrder.toDTO();
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (order.getStatus() == OrderDTO.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        order.updateStatus(OrderDTO.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public long getOrderCountByRestaurantAndStatus(Long restaurantId, OrderDTO.OrderStatus status) {
        return orderRepository.countByRestaurantAndStatus(restaurantId, status);
    }

    public long getOrderCountByCustomer(Long customerId) {
        return orderRepository.countByCustomer(customerId);
    }

    public boolean isOrderFromRestaurant(Long orderId, Long restaurantId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getRestaurantId().equals(restaurantId))
                .orElse(false);
    }

    public boolean isOrderFromCustomer(Long orderId, Long customerId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomerId().equals(customerId))
                .orElse(false);
    }
}
