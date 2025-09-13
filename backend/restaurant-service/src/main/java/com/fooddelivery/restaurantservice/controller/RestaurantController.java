package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.common.dto.RestaurantDTO;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import com.fooddelivery.restaurantservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createRestaurant(@Valid @RequestBody RestaurantDTO restaurantDTO,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"RESTAURANT".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only restaurant owners and admins can create restaurants");
            }

            restaurantDTO.setOwnerId(userId);
            RestaurantDTO createdRestaurant = restaurantService.createRestaurant(restaurantDTO);
            return ResponseEntity.ok(createdRestaurant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create restaurant: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getApprovedRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(@PathVariable Long id) {
        try {
            RestaurantDTO restaurant = restaurantService.getRestaurantById(id)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            return ResponseEntity.ok(restaurant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get restaurant: " + e.getMessage());
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByOwner(@PathVariable Long ownerId) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/my-restaurants")
    public ResponseEntity<?> getMyRestaurants(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByOwner(userId);
            return ResponseEntity.ok(restaurants);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get restaurants: " + e.getMessage());
        }
    }

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByCuisine(@PathVariable String cuisineType) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByCuisine(cuisineType);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantDTO>> searchRestaurants(@RequestParam String name) {
        List<RestaurantDTO> restaurants = restaurantService.searchRestaurantsByName(name);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/location")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByLocation(@RequestParam String location) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByLocation(location);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<RestaurantDTO>> getTopRatedRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getTopRatedRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id,
                                            @Valid @RequestBody RestaurantDTO restaurantDTO,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to update this restaurant");
            }

            RestaurantDTO updatedRestaurant = restaurantService.updateRestaurant(id, restaurantDTO);
            return ResponseEntity.ok(updatedRestaurant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update restaurant: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<?> updateRating(@PathVariable Long id,
                                        @RequestParam Double rating,
                                        @RequestParam Integer totalReviews) {
        try {
            restaurantService.updateRating(id, rating, totalReviews);
            return ResponseEntity.ok("Rating updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update rating: " + e.getMessage());
        }
    }
}
