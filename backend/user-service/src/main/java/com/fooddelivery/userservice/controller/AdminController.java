package com.fooddelivery.userservice.controller;

import com.fooddelivery.common.dto.UserDTO;
import com.fooddelivery.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable UserDTO.UserRole role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        List<UserDTO> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String name) {
        List<UserDTO> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok("User activated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to activate user: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok("User deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to deactivate user: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = Map.of(
            "totalCustomers", userService.countUsersByRole(UserDTO.UserRole.CUSTOMER),
            "totalRestaurants", userService.countUsersByRole(UserDTO.UserRole.RESTAURANT),
            "totalDeliveryPersonnel", userService.countUsersByRole(UserDTO.UserRole.DELIVERY),
            "totalAdmins", userService.countUsersByRole(UserDTO.UserRole.ADMIN)
        );
        return ResponseEntity.ok(stats);
    }
}
