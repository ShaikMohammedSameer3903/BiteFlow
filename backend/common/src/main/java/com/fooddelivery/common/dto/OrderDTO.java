package com.fooddelivery.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private Long id;
    
    @NotNull
    private Long customerId;
    
    @NotNull
    private Long restaurantId;
    
    @NotNull
    private List<OrderItemDTO> items;
    
    @NotNull
    @Positive
    private BigDecimal totalAmount;
    
    @NotNull
    private OrderStatus status;
    
    private String deliveryAddress;
    
    private String specialInstructions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private Long deliveryId;
    
    private Long paymentId;

    // Constructors
    public OrderDTO() {}

    public OrderDTO(Long customerId, Long restaurantId, List<OrderItemDTO> items, 
                   BigDecimal totalAmount, String deliveryAddress) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }

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

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PREPARING,
        READY_FOR_PICKUP,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
}
