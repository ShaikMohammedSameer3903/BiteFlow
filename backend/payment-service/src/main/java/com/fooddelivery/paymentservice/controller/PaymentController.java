package com.fooddelivery.paymentservice.controller;

import com.fooddelivery.common.dto.PaymentDTO;
import com.fooddelivery.paymentservice.service.PaymentService;
import com.fooddelivery.paymentservice.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/stripe")
    public ResponseEntity<?> processStripePayment(@RequestParam Long orderId,
                                                @RequestParam BigDecimal amount,
                                                @RequestParam String paymentMethodId,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long customerId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only customers can make payments");
            }

            PaymentDTO payment = paymentService.processStripePayment(orderId, amount, paymentMethodId, customerId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment processing failed: " + e.getMessage());
        }
    }

    @PostMapping("/cash-on-delivery")
    public ResponseEntity<?> processCashOnDeliveryPayment(@RequestParam Long orderId,
                                                        @RequestParam BigDecimal amount,
                                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long customerId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only customers can create payments");
            }

            PaymentDTO payment = paymentService.processCashOnDeliveryPayment(orderId, amount, customerId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm-cash")
    public ResponseEntity<?> confirmCashPayment(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"DELIVERY".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only delivery personnel and admins can confirm cash payments");
            }

            PaymentDTO payment = paymentService.confirmCashPayment(id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to confirm payment: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            PaymentDTO payment = paymentService.getPaymentById(id)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (!"ADMIN".equals(role) && !paymentService.isPaymentFromCustomer(id, userId)) {
                return ResponseEntity.badRequest().body("Not authorized to view this payment");
            }

            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payment: " + e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            // Additional authorization checks would be needed here to verify order ownership
            if (!"CUSTOMER".equals(role) && !"RESTAURANT".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Not authorized to view payment for this order");
            }

            PaymentDTO payment = paymentService.getPaymentByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for order"));
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payment: " + e.getMessage());
        }
    }

    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPayments(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long customerId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"CUSTOMER".equals(role)) {
                return ResponseEntity.badRequest().body("Only customers can view their payments");
            }

            List<PaymentDTO> payments = paymentService.getPaymentsByCustomer(customerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payments: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getPaymentsByCustomer(@PathVariable Long customerId,
                                                  @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role) && !userId.equals(customerId)) {
                return ResponseEntity.badRequest().body("Not authorized to view these payments");
            }

            List<PaymentDTO> payments = paymentService.getPaymentsByCustomer(customerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payments: " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable PaymentDTO.PaymentStatus status,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can view payments by status");
            }

            List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payments: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long id,
                                         @RequestParam BigDecimal refundAmount,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can process refunds");
            }

            PaymentDTO payment = paymentService.refundPayment(id, refundAmount);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Refund failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id,
                                               @RequestParam PaymentDTO.PaymentStatus status,
                                               @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can update payment status");
            }

            PaymentDTO payment = paymentService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update payment status: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getPaymentStats(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can view payment statistics");
            }

            Map<String, Object> stats = paymentService.getPaymentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get payment stats: " + e.getMessage());
        }
    }

    @PostMapping("/process-expired")
    public ResponseEntity<?> processExpiredPayments(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String role = jwtUtil.extractRole(token);

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.badRequest().body("Only admins can process expired payments");
            }

            paymentService.processExpiredPayments();
            return ResponseEntity.ok("Expired payments processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process expired payments: " + e.getMessage());
        }
    }
}
