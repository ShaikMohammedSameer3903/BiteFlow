package com.fooddelivery.restaurantservice.entity;

import com.fooddelivery.common.dto.MenuItemDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    private boolean available = true;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String allergens;

    @Column(name = "preparation_time")
    private Integer preparationTime;

    // Constructors
    public MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price, Long restaurantId, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.restaurantId = restaurantId;
        this.category = category;
        this.available = true;
    }

    // Convert to DTO
    public MenuItemDTO toDTO() {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setPrice(this.price);
        dto.setRestaurantId(this.restaurantId);
        dto.setCategory(this.category);
        dto.setImageUrl(this.imageUrl);
        dto.setAvailable(this.available);
        dto.setIngredients(this.ingredients);
        dto.setAllergens(this.allergens);
        dto.setPreparationTime(this.preparationTime);
        return dto;
    }

    // Update from DTO
    public void updateFromDTO(MenuItemDTO dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getPrice() != null) this.price = dto.getPrice();
        if (dto.getCategory() != null) this.category = dto.getCategory();
        if (dto.getImageUrl() != null) this.imageUrl = dto.getImageUrl();
        if (dto.getIngredients() != null) this.ingredients = dto.getIngredients();
        if (dto.getAllergens() != null) this.allergens = dto.getAllergens();
        if (dto.getPreparationTime() != null) this.preparationTime = dto.getPreparationTime();
        this.available = dto.isAvailable();
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
