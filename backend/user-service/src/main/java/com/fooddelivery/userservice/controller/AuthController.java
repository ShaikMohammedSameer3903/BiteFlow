package com.fooddelivery.userservice.controller;

import com.fooddelivery.common.dto.UserDTO;
import com.fooddelivery.userservice.dto.AuthRequest;
import com.fooddelivery.userservice.dto.AuthResponse;
import com.fooddelivery.userservice.dto.RegisterRequest;
import com.fooddelivery.userservice.service.UserService;
import com.fooddelivery.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserDTO user = userService.getUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());
            
            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.emailExists(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            UserDTO user = userService.createUser(
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getName(),
                registerRequest.getRole()
            );

            // Update additional fields if provided
            if (registerRequest.getPhone() != null || registerRequest.getAddress() != null) {
                UserDTO updateDTO = new UserDTO();
                updateDTO.setPhone(registerRequest.getPhone());
                updateDTO.setAddress(registerRequest.getAddress());
                user = userService.updateUser(user.getId(), updateDTO);
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractUsername(token);
                    UserDTO user = userService.getUserByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return ResponseEntity.ok(user);
                }
            }
            return ResponseEntity.badRequest().body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token validation failed");
        }
    }
}
