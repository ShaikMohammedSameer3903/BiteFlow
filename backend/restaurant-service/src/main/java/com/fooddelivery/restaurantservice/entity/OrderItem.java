package com.fooddelivery.restaurantservice.entity;

import com.fooddelivery.common.dto.OrderItemDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // Constructors
    public OrderItem() {}

    public OrderItem(Long menuItemId, String itemName, Integer quantity, BigDecimal unitPrice) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Convert to DTO
    public OrderItemDTO toDTO() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(this.id);
        dto.setMenuItemId(this.menuItemId);
        dto.setItemName(this.itemName);
        dto.setQuantity(this.quantity);
        dto.setUnitPrice(this.unitPrice);
        dto.setTotalPrice(this.totalPrice);
        dto.setSpecialRequests(this.specialRequests);
        return dto;
    }

    // Update quantity and recalculate total
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

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
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
