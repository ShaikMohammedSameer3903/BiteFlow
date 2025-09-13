package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.common.dto.RestaurantDTO;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public RestaurantDTO createRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = new Restaurant(
            restaurantDTO.getName(),
            restaurantDTO.getAddress(),
            restaurantDTO.getCuisineType(),
            restaurantDTO.getOwnerId()
        );
        
        restaurant.updateFromDTO(restaurantDTO);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return savedRestaurant.toDTO();
    }

    public Optional<RestaurantDTO> getRestaurantById(Long id) {
        return restaurantRepository.findById(id).map(Restaurant::toDTO);
    }

    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getActiveRestaurants() {
        return restaurantRepository.findByActiveTrue().stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getApprovedRestaurants() {
        return restaurantRepository.findByActiveTrueAndApprovedTrue().stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getRestaurantsByCuisine(String cuisineType) {
        return restaurantRepository.findActiveByCuisineType(cuisineType).stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> searchRestaurantsByName(String name) {
        return restaurantRepository.findActiveByNameContaining(name).stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getRestaurantsByLocation(String location) {
        return restaurantRepository.findActiveByLocation(location).stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getRestaurantsByMinRating(Double minRating) {
        return restaurantRepository.findActiveByMinRating(minRating).stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getTopRatedRestaurants() {
        return restaurantRepository.findActiveOrderByRatingDesc().stream()
                .map(Restaurant::toDTO)
                .collect(Collectors.toList());
    }

    public RestaurantDTO updateRestaurant(Long id, RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));

        restaurant.updateFromDTO(restaurantDTO);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return updatedRestaurant.toDTO();
    }

    public void approveRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        
        restaurant.setApproved(true);
        restaurantRepository.save(restaurant);
    }

    public void rejectRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        
        restaurant.setApproved(false);
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    public void activateRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        
        restaurant.setActive(true);
        restaurantRepository.save(restaurant);
    }

    public void deactivateRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    public void updateRating(Long id, Double newRating, Integer totalReviews) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
        
        restaurant.setRating(newRating);
        restaurant.setTotalReviews(totalReviews);
        restaurantRepository.save(restaurant);
    }

    public long getActiveRestaurantCount() {
        return restaurantRepository.countActiveApprovedRestaurants();
    }

    public long getPendingApprovalCount() {
        return restaurantRepository.countPendingApproval();
    }

    public boolean isRestaurantOwner(Long restaurantId, Long userId) {
        return restaurantRepository.findById(restaurantId)
                .map(restaurant -> restaurant.getOwnerId().equals(userId))
                .orElse(false);
    }
}
