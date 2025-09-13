package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.common.dto.OrderDTO;
import com.fooddelivery.restaurantservice.service.OrderService;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import com.fooddelivery.restaurantservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDTO orderDTO,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only customers can create orders");
            }

            orderDTO.setCustomerId(userId);
            OrderDTO createdOrder = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            OrderDTO order = orderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Check authorization
            if (!"ADMIN".equals(role) && 
                !orderService.isOrderFromCustomer(id, userId) && 
                !restaurantService.isRestaurantOwner(order.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view this order");
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get order: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable Long customerId,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !userId.equals(customerId)) {
                return ResponseEntity.badRequest().body("Not authorized to view these orders");
            }

            List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get orders: " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"CUSTOMER".equals(role)) {
                return ResponseEntity.badRequest().body("Only customers can view their orders");
            }

            List<OrderDTO> orders = orderService.getOrdersByCustomer(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get orders: " + e.getMessage());
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getOrdersByRestaurant(@PathVariable Long restaurantId,
                                                  @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(restaurantId, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view these orders");
            }

            List<OrderDTO> orders = orderService.getOrdersByRestaurant(restaurantId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get orders: " + e.getMessage());
        }
    }

    @GetMapping("/restaurant/{restaurantId}/active")
    public ResponseEntity<?> getActiveOrdersByRestaurant(@PathVariable Long restaurantId,
                                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(restaurantId, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view these orders");
            }

            List<OrderDTO> orders = orderService.getActiveOrdersByRestaurant(restaurantId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get active orders: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                             @RequestParam OrderDTO.OrderStatus status,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            OrderDTO order = orderService.getOrderById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!"ADMIN".equals(role) && !restaurantService.isRestaurantOwner(order.getRestaurantId(), userId)) {
                return ResponseEntity.badRequest().body("Not authorized to update this order");
            }

            OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update order status: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !orderService.isOrderFromCustomer(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to cancel this order");
            }

            orderService.cancelOrder(id);
            return ResponseEntity.ok("Order cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel order: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderDTO.OrderStatus status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
