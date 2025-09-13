package com.fooddelivery.deliveryservice.controller;

import com.fooddelivery.common.dto.DeliveryDTO;
import com.fooddelivery.deliveryservice.service.DeliveryService;
import com.fooddelivery.deliveryservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !"RESTAURANT".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins and restaurants can create deliveries");
            }

            DeliveryDTO createdDelivery = deliveryService.createDelivery(deliveryDTO);
            return ResponseEntity.ok(createdDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create delivery: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryById(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            DeliveryDTO delivery = deliveryService.getDeliveryById(id)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            // Check authorization
            if (!"ADMIN".equals(role) && 
                !"DELIVERY".equals(role) && 
                !deliveryService.isDeliveryAssignedToPerson(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view this delivery");
            }

            return ResponseEntity.ok(delivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get delivery: " + e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDeliveryByOrderId(@PathVariable Long orderId) {
        try {
            DeliveryDTO delivery = deliveryService.getDeliveryByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found for order"));
            return ResponseEntity.ok(delivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get delivery: " + e.getMessage());
        }
    }

    @GetMapping("/track/{trackingCode}")
    public ResponseEntity<?> trackDelivery(@PathVariable String trackingCode) {
        try {
            DeliveryDTO delivery = deliveryService.getDeliveryByTrackingCode(trackingCode)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));
            return ResponseEntity.ok(delivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to track delivery: " + e.getMessage());
        }
    }

    @GetMapping("/my-deliveries")
    public ResponseEntity<?> getMyDeliveries(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Only delivery personnel can view their deliveries");
            }

            List<DeliveryDTO> deliveries = deliveryService.getDeliveriesByPerson(userId);
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get deliveries: " + e.getMessage());
        }
    }

    @GetMapping("/my-active-deliveries")
    public ResponseEntity<?> getMyActiveDeliveries(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Only delivery personnel can view their active deliveries");
            }

            List<DeliveryDTO> deliveries = deliveryService.getActiveDeliveriesByPerson(userId);
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get active deliveries: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDeliveries(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Not authorized to view pending deliveries");
            }

            List<DeliveryDTO> deliveries = deliveryService.getPendingDeliveries();
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get pending deliveries: " + e.getMessage());
        }
    }

    @GetMapping("/unassigned")
    public ResponseEntity<?> getUnassignedDeliveries(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Not authorized to view unassigned deliveries");
            }

            List<DeliveryDTO> deliveries = deliveryService.getUnassignedDeliveries();
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get unassigned deliveries: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignDelivery(@PathVariable Long id,
                                          @RequestParam Long deliveryPersonId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can assign deliveries");
            }

            DeliveryDTO updatedDelivery = deliveryService.assignDeliveryPerson(id, deliveryPersonId);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to assign delivery: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Only delivery personnel can accept deliveries");
            }

            DeliveryDTO updatedDelivery = deliveryService.assignDeliveryPerson(id, userId);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to accept delivery: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id,
                                                @RequestParam DeliveryDTO.DeliveryStatus status,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && 
                (!"DELIVERY".equals(role) || !deliveryService.isDeliveryAssignedToPerson(id, userId))) {
                return ResponseEntity.badRequest().body("Not authorized to update this delivery");
            }

            DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, status);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update delivery status: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id,
                                          @RequestParam Double latitude,
                                          @RequestParam Double longitude,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role) || !deliveryService.isDeliveryAssignedToPerson(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to update location for this delivery");
            }

            DeliveryDTO updatedDelivery = deliveryService.updateLocation(id, latitude, longitude);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update location: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estimated-time")
    public ResponseEntity<?> updateEstimatedTime(@PathVariable Long id,
                                               @RequestParam String estimatedTime,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && 
                (!"DELIVERY".equals(role) || !deliveryService.isDeliveryAssignedToPerson(id, userId))) {
                return ResponseEntity.badRequest().body("Not authorized to update estimated time");
            }

            LocalDateTime estimatedDateTime = LocalDateTime.parse(estimatedTime);
            DeliveryDTO updatedDelivery = deliveryService.updateEstimatedTime(id, estimatedDateTime);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update estimated time: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<?> addDeliveryNotes(@PathVariable Long id,
                                            @RequestParam String notes,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role) || !deliveryService.isDeliveryAssignedToPerson(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to add notes to this delivery");
            }

            DeliveryDTO updatedDelivery = deliveryService.addDeliveryNotes(id, notes);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add delivery notes: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can cancel deliveries");
            }

            deliveryService.cancelDelivery(id);
            return ResponseEntity.ok("Delivery cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel delivery: " + e.getMessage());
        }
    }

    @GetMapping("/optimal")
    public ResponseEntity<?> getOptimalDeliveries(@RequestParam Double latitude,
                                                @RequestParam Double longitude,
                                                @RequestParam(defaultValue = "5") Integer maxDeliveries,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role)) {
                return ResponseEntity.badRequest().body("Only delivery personnel can get optimal deliveries");
            }

            List<DeliveryDTO> deliveries = deliveryService.findOptimalDeliveries(userId, latitude, longitude, maxDeliveries);
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get optimal deliveries: " + e.getMessage());
        }
    }
}
