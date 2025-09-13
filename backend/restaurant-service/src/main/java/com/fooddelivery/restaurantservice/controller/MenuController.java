package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.common.dto.MenuItemDTO;
import com.fooddelivery.restaurantservice.service.MenuService;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import com.fooddelivery.restaurantservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createMenuItem(@Valid @RequestBody MenuItemDTO menuItemDTO,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(menuItemDTO.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to add menu items to this restaurant");
            }

            MenuItemDTO createdMenuItem = menuService.createMenuItem(menuItemDTO);
            return ResponseEntity.ok(createdMenuItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create menu item: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItemDTO menuItem = menuService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));
            return ResponseEntity.ok(menuItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get menu item: " + e.getMessage());
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemDTO>> getMenuByRestaurant(@PathVariable Long restaurantId) {
        List<MenuItemDTO> menuItems = menuService.getAvailableMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/restaurant/{restaurantId}/all")
    public ResponseEntity<?> getAllMenuByRestaurant(@PathVariable Long restaurantId,
                                                   @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(restaurantId, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view all menu items");
            }

            List<MenuItemDTO> menuItems = menuService.getMenuItemsByRestaurant(restaurantId);
            return ResponseEntity.ok(menuItems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get menu items: " + e.getMessage());
        }
    }

    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    public ResponseEntity<List<MenuItemDTO>> getMenuByCategory(@PathVariable Long restaurantId,
                                                             @PathVariable String category) {
        List<MenuItemDTO> menuItems = menuService.getMenuItemsByRestaurantAndCategory(restaurantId, category);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/restaurant/{restaurantId}/categories")
    public ResponseEntity<List<String>> getCategoriesByRestaurant(@PathVariable Long restaurantId) {
        List<String> categories = menuService.getCategoriesByRestaurant(restaurantId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/restaurant/{restaurantId}/search")
    public ResponseEntity<List<MenuItemDTO>> searchMenuItems(@PathVariable Long restaurantId,
                                                           @RequestParam String name) {
        List<MenuItemDTO> menuItems = menuService.searchMenuItems(restaurantId, name);
        return ResponseEntity.ok(menuItems);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id,
                                          @Valid @RequestBody MenuItemDTO menuItemDTO,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            MenuItemDTO existingItem = menuService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(existingItem.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to update this menu item");
            }

            MenuItemDTO updatedMenuItem = menuService.updateMenuItem(id, menuItemDTO);
            return ResponseEntity.ok(updatedMenuItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update menu item: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long id,
                                              @RequestParam boolean available,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            MenuItemDTO existingItem = menuService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(existingItem.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to update this menu item");
            }

            menuService.setMenuItemAvailability(id, available);
            return ResponseEntity.ok("Availability updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update availability: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            MenuItemDTO existingItem = menuService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(existingItem.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to delete this menu item");
            }

            menuService.deleteMenuItem(id);
            return ResponseEntity.ok("Menu item deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete menu item: " + e.getMessage());
        }
    }
}
