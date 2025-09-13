package com.fooddelivery.restaurantservice.entity;

import com.fooddelivery.common.dto.RestaurantDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private String phone;

    private String email;

    @Column(name = "cuisine_type", nullable = false)
    private String cuisineType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    private Double rating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "delivery_fee")
    private Double deliveryFee;

    @Column(name = "estimated_delivery_time")
    private Integer estimatedDeliveryTime;

    private boolean active = true;

    private boolean approved = false;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String address, String cuisineType, Long ownerId) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.ownerId = ownerId;
        this.active = true;
        this.approved = false;
        this.rating = 0.0;
        this.totalReviews = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Convert to DTO
    public RestaurantDTO toDTO() {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setAddress(this.address);
        dto.setPhone(this.phone);
        dto.setEmail(this.email);
        dto.setCuisineType(this.cuisineType);
        dto.setDescription(this.description);
        dto.setImageUrl(this.imageUrl);
        dto.setOpeningTime(this.openingTime);
        dto.setClosingTime(this.closingTime);
        dto.setRating(this.rating);
        dto.setTotalReviews(this.totalReviews);
        dto.setDeliveryFee(this.deliveryFee);
        dto.setEstimatedDeliveryTime(this.estimatedDeliveryTime);
        dto.setActive(this.active);
        dto.setApproved(this.approved);
        dto.setOwnerId(this.ownerId);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        return dto;
    }

    // Update from DTO
    public void updateFromDTO(RestaurantDTO dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getAddress() != null) this.address = dto.getAddress();
        if (dto.getPhone() != null) this.phone = dto.getPhone();
        if (dto.getEmail() != null) this.email = dto.getEmail();
        if (dto.getCuisineType() != null) this.cuisineType = dto.getCuisineType();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getImageUrl() != null) this.imageUrl = dto.getImageUrl();
        if (dto.getOpeningTime() != null) this.openingTime = dto.getOpeningTime();
        if (dto.getClosingTime() != null) this.closingTime = dto.getClosingTime();
        if (dto.getDeliveryFee() != null) this.deliveryFee = dto.getDeliveryFee();
        if (dto.getEstimatedDeliveryTime() != null) this.estimatedDeliveryTime = dto.getEstimatedDeliveryTime();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalTime getOpeningTime() { return openingTime; }
    public void setOpeningTime(LocalTime openingTime) { this.openingTime = openingTime; }

    public LocalTime getClosingTime() { return closingTime; }
    public void setClosingTime(LocalTime closingTime) { this.closingTime = closingTime; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }

    public Integer getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(Integer estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
