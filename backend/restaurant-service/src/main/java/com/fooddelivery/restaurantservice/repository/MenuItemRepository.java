package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByRestaurantId(Long restaurantId);
    
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
    
    List<MenuItem> findByCategory(String category);
    
    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.available = true AND m.category = :category")
    List<MenuItem> findAvailableByRestaurantAndCategory(@Param("restaurantId") Long restaurantId, @Param("category") String category);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.available = true AND m.name ILIKE %:name%")
    List<MenuItem> findAvailableByRestaurantAndNameContaining(@Param("restaurantId") Long restaurantId, @Param("name") String name);
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.available = true")
    List<String> findCategoriesByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.restaurantId = :restaurantId AND m.available = true")
    long countAvailableByRestaurant(@Param("restaurantId") Long restaurantId);
}
