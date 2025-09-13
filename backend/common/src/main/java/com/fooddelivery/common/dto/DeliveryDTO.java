package com.fooddelivery.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DeliveryDTO {
    private Long id;
    
    @NotNull
    private Long orderId;
    
    private Long deliveryPersonId;
    
    @NotNull
    private DeliveryStatus status;
    
    private String pickupAddress;
    
    private String deliveryAddress;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickedUpAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    
    private Double currentLatitude;
    
    private Double currentLongitude;
    
    private String deliveryNotes;
    
    private String trackingCode;

    // Constructors
    public DeliveryDTO() {}

    public DeliveryDTO(Long orderId, String pickupAddress, String deliveryAddress) {
        this.orderId = orderId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryStatus.PENDING;
        this.trackingCode = generateTrackingCode();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(Long deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public String getTrackingCode() { return trackingCode; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }

    private String generateTrackingCode() {
        return "TRK" + System.currentTimeMillis();
    }

    public enum DeliveryStatus {
        PENDING,
        ASSIGNED,
        PICKED_UP,
        OUT_FOR_DELIVERY,
        DELIVERED,
        CANCELLED,
        FAILED
    }
}
