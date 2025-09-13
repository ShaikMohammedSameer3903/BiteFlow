package com.fooddelivery.userservice.controller;

import com.fooddelivery.common.dto.UserDTO;
import com.fooddelivery.userservice.dto.ChangePasswordRequest;
import com.fooddelivery.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserDTO user = userService.getUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get profile: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserDTO userDTO, Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserDTO updatedUser = userService.updateUserProfile(userDetails.getUsername(), userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update profile: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, 
                                          Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            boolean success = userService.changePassword(
                userDetails.getUsername(), 
                request.getOldPassword(), 
                request.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok("Password changed successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid old password");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to change password: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get user: " + e.getMessage());
        }
    }
}
