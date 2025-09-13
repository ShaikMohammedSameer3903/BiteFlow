package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByActiveTrue();
    
    List<Restaurant> findByApprovedTrue();
    
    List<Restaurant> findByActiveTrueAndApprovedTrue();
    
    List<Restaurant> findByOwnerId(Long ownerId);
    
    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.approved = true AND r.cuisineType ILIKE %:cuisineType%")
    List<Restaurant> findActiveByCuisineType(@Param("cuisineType") String cuisineType);
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.approved = true AND r.name ILIKE %:name%")
    List<Restaurant> findActiveByNameContaining(@Param("name") String name);
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.approved = true AND r.address ILIKE %:location%")
    List<Restaurant> findActiveByLocation(@Param("location") String location);
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.approved = true AND r.rating >= :minRating ORDER BY r.rating DESC")
    List<Restaurant> findActiveByMinRating(@Param("minRating") Double minRating);
    
    @Query("SELECT r FROM Restaurant r WHERE r.active = true AND r.approved = true ORDER BY r.rating DESC")
    List<Restaurant> findActiveOrderByRatingDesc();
    
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.active = true AND r.approved = true")
    long countActiveApprovedRestaurants();
    
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.approved = false")
    long countPendingApproval();
}
