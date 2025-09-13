package com.fooddelivery.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long id;
    
    @NotNull
    private Long menuItemId;
    
    @NotNull
    private String itemName;
    
    @NotNull
    @Positive
    private Integer quantity;
    
    @NotNull
    @Positive
    private BigDecimal unitPrice;
    
    @NotNull
    @Positive
    private BigDecimal totalPrice;
    
    private String specialRequests;

    // Constructors
    public OrderItemDTO() {}

    public OrderItemDTO(Long menuItemId, String itemName, Integer quantity, BigDecimal unitPrice) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        if (this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice;
        if (this.quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
