package com.fooddelivery.deliveryservice.entity;

import com.fooddelivery.common.dto.DeliveryDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "delivery_person_id")
    private Long deliveryPersonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryDTO.DeliveryStatus status;

    @Column(name = "pickup_address", columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "tracking_code", unique = true)
    private String trackingCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Delivery() {}

    public Delivery(Long orderId, String pickupAddress, String deliveryAddress) {
        this.orderId = orderId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryDTO.DeliveryStatus.PENDING;
        this.trackingCode = generateTrackingCode();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Convert to DTO
    public DeliveryDTO toDTO() {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(this.id);
        dto.setOrderId(this.orderId);
        dto.setDeliveryPersonId(this.deliveryPersonId);
        dto.setStatus(this.status);
        dto.setPickupAddress(this.pickupAddress);
        dto.setDeliveryAddress(this.deliveryAddress);
        dto.setAssignedAt(this.assignedAt);
        dto.setPickedUpAt(this.pickedUpAt);
        dto.setDeliveredAt(this.deliveredAt);
        dto.setEstimatedDeliveryTime(this.estimatedDeliveryTime);
        dto.setCurrentLatitude(this.currentLatitude);
        dto.setCurrentLongitude(this.currentLongitude);
        dto.setDeliveryNotes(this.deliveryNotes);
        dto.setTrackingCode(this.trackingCode);
        return dto;
    }

    // Update status
    public void updateStatus(DeliveryDTO.DeliveryStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        
        switch (newStatus) {
            case ASSIGNED:
                this.assignedAt = LocalDateTime.now();
                break;
            case PICKED_UP:
                this.pickedUpAt = LocalDateTime.now();
                break;
            case DELIVERED:
                this.deliveredAt = LocalDateTime.now();
                break;
        }
    }

    // Update location
    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.updatedAt = LocalDateTime.now();
    }

    // Generate tracking code
    private String generateTrackingCode() {
        return "TRK" + System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(Long deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }

    public DeliveryDTO.DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryDTO.DeliveryStatus status) { this.status = status; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
