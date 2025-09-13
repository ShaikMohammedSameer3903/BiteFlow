package com.fooddelivery.restaurantservice.entity;

import com.fooddelivery.common.dto.OrderDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderDTO.OrderStatus status;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delivery_id")
    private Long deliveryId;

    @Column(name = "payment_id")
    private Long paymentId;

    // Constructors
    public Order() {}

    public Order(Long customerId, Long restaurantId, BigDecimal totalAmount, String deliveryAddress) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderDTO.OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Convert to DTO
    public OrderDTO toDTO() {
        OrderDTO dto = new OrderDTO();
        dto.setId(this.id);
        dto.setCustomerId(this.customerId);
        dto.setRestaurantId(this.restaurantId);
        dto.setItems(this.items.stream().map(OrderItem::toDTO).collect(Collectors.toList()));
        dto.setTotalAmount(this.totalAmount);
        dto.setStatus(this.status);
        dto.setDeliveryAddress(this.deliveryAddress);
        dto.setSpecialInstructions(this.specialInstructions);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        dto.setDeliveryId(this.deliveryId);
        dto.setPaymentId(this.paymentId);
        return dto;
    }

    // Update status
    public void updateStatus(OrderDTO.OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    // Add order item
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Remove order item
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // Calculate total amount
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderDTO.OrderStatus getStatus() { return status; }
    public void setStatus(OrderDTO.OrderStatus status) { this.status = status; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
}
