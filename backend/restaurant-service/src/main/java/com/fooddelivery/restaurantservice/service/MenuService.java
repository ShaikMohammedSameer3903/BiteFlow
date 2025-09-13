package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.common.dto.MenuItemDTO;
import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public MenuItemDTO createMenuItem(MenuItemDTO menuItemDTO) {
        MenuItem menuItem = new MenuItem(
            menuItemDTO.getName(),
            menuItemDTO.getDescription(),
            menuItemDTO.getPrice(),
            menuItemDTO.getRestaurantId(),
            menuItemDTO.getCategory()
        );
        
        menuItem.updateFromDTO(menuItemDTO);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return savedMenuItem.toDTO();
    }

    public Optional<MenuItemDTO> getMenuItemById(Long id) {
        return menuItemRepository.findById(id).map(MenuItem::toDTO);
    }

    public List<MenuItemDTO> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(MenuItem::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAvailableMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(MenuItem::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getMenuItemsByCategory(String category) {
        return menuItemRepository.findByCategory(category).stream()
                .map(MenuItem::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getMenuItemsByRestaurantAndCategory(Long restaurantId, String category) {
        return menuItemRepository.findAvailableByRestaurantAndCategory(restaurantId, category).stream()
                .map(MenuItem::toDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> searchMenuItems(Long restaurantId, String name) {
        return menuItemRepository.findAvailableByRestaurantAndNameContaining(restaurantId, name).stream()
                .map(MenuItem::toDTO)
                .collect(Collectors.toList());
    }

    public List<String> getCategoriesByRestaurant(Long restaurantId) {
        return menuItemRepository.findCategoriesByRestaurant(restaurantId);
    }

    public MenuItemDTO updateMenuItem(Long id, MenuItemDTO menuItemDTO) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));

        menuItem.updateFromDTO(menuItemDTO);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return updatedMenuItem.toDTO();
    }

    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Menu item not found: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    public void setMenuItemAvailability(Long id, boolean available) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + id));
        
        menuItem.setAvailable(available);
        menuItemRepository.save(menuItem);
    }

    public long getAvailableItemCountByRestaurant(Long restaurantId) {
        return menuItemRepository.countAvailableByRestaurant(restaurantId);
    }

    public boolean isMenuItemFromRestaurant(Long menuItemId, Long restaurantId) {
        return menuItemRepository.findById(menuItemId)
                .map(menuItem -> menuItem.getRestaurantId().equals(restaurantId))
                .orElse(false);
    }
}
