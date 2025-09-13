package com.fooddelivery.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class MenuItemDTO {
    private Long id;
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @NotNull
    private Long restaurantId;
    
    private String category;
    
    private String imageUrl;
    
    private boolean available;
    
    private String ingredients;
    
    private String allergens;
    
    private Integer preparationTime; // in minutes

    // Constructors
    public MenuItemDTO() {}

    public MenuItemDTO(String name, String description, BigDecimal price, Long restaurantId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.restaurantId = restaurantId;
        this.category = category;
        this.available = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
}
