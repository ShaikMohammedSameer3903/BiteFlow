package com.fooddelivery.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class RestaurantDTO {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String address;
    
    private String phone;
    
    private String email;
    
    @NotNull
    private String cuisineType;
    
    private String description;
    
    private String imageUrl;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openingTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closingTime;
    
    private Double rating;
    
    private Integer totalReviews;
    
    private Double deliveryFee;
    
    private Integer estimatedDeliveryTime; // in minutes
    
    private boolean active;
    
    private boolean approved;
    
    @NotNull
    private Long ownerId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public RestaurantDTO() {}

    public RestaurantDTO(String name, String address, String cuisineType, Long ownerId) {
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
