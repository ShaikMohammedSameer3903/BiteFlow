package com.fooddelivery.paymentservice.service;

import com.fooddelivery.common.dto.PaymentDTO;
import com.fooddelivery.paymentservice.entity.Payment;
import com.fooddelivery.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment(
            paymentDTO.getOrderId(),
            paymentDTO.getAmount(),
            paymentDTO.getPaymentMethod(),
            extractCustomerIdFromContext() // This would be extracted from JWT in real implementation
        );

        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment.toDTO();
    }

    public PaymentDTO processStripePayment(Long orderId, BigDecimal amount, String paymentMethodId, Long customerId) {
        try {
            // Create payment record
            Payment payment = new Payment(orderId, amount, PaymentDTO.PaymentMethod.CREDIT_CARD, customerId);
            payment.updateStatus(PaymentDTO.PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);

            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                .setCurrency("usd")
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setReturnUrl("https://your-domain.com/return")
                .putMetadata("order_id", orderId.toString())
                .putMetadata("payment_id", payment.getId().toString())
                .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Update payment with Stripe details
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setTransactionId(paymentIntent.getId());
            payment.setGatewayResponse(paymentIntent.toJson());

            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.updateStatus(PaymentDTO.PaymentStatus.COMPLETED);
            } else if ("requires_action".equals(paymentIntent.getStatus())) {
                payment.updateStatus(PaymentDTO.PaymentStatus.PENDING);
            } else {
                payment.updateStatus(PaymentDTO.PaymentStatus.FAILED);
                payment.setFailureReason("Payment failed: " + paymentIntent.getStatus());
            }

            Payment updatedPayment = paymentRepository.save(payment);
            return updatedPayment.toDTO();

        } catch (StripeException e) {
            return handlePaymentFailure(orderId, amount, customerId, e.getMessage());
        }
    }

    public PaymentDTO processCashOnDeliveryPayment(Long orderId, BigDecimal amount, Long customerId) {
        Payment payment = new Payment(orderId, amount, PaymentDTO.PaymentMethod.CASH_ON_DELIVERY, customerId);
        payment.updateStatus(PaymentDTO.PaymentStatus.PENDING);
        payment.setTransactionId("COD_" + System.currentTimeMillis());
        
        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment.toDTO();
    }

    public PaymentDTO confirmCashPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getPaymentMethod() != PaymentDTO.PaymentMethod.CASH_ON_DELIVERY) {
            throw new RuntimeException("This method is only for cash on delivery payments");
        }

        payment.updateStatus(PaymentDTO.PaymentStatus.COMPLETED);
        Payment updatedPayment = paymentRepository.save(payment);
        return updatedPayment.toDTO();
    }

    private PaymentDTO handlePaymentFailure(Long orderId, BigDecimal amount, Long customerId, String errorMessage) {
        Payment payment = new Payment(orderId, amount, PaymentDTO.PaymentMethod.CREDIT_CARD, customerId);
        payment.updateStatus(PaymentDTO.PaymentStatus.FAILED);
        payment.setFailureReason(errorMessage);
        
        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment.toDTO();
    }

    public Optional<PaymentDTO> getPaymentById(Long id) {
        return paymentRepository.findById(id).map(Payment::toDTO);
    }

    public Optional<PaymentDTO> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).map(Payment::toDTO);
    }

    public List<PaymentDTO> getPaymentsByCustomer(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(Payment::toDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsByStatus(PaymentDTO.PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(Payment::toDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO refundPayment(Long paymentId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentDTO.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Can only refund completed payments");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed payment amount");
        }

        try {
            if (payment.getStripePaymentIntentId() != null) {
                // Process Stripe refund
                RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue())
                    .build();

                Refund refund = Refund.create(params);
                payment.setGatewayResponse(payment.getGatewayResponse() + "\nRefund: " + refund.toJson());
            }

            payment.processRefund(refundAmount);
            Payment updatedPayment = paymentRepository.save(payment);
            return updatedPayment.toDTO();

        } catch (StripeException e) {
            throw new RuntimeException("Refund failed: " + e.getMessage());
        }
    }

    public PaymentDTO updatePaymentStatus(Long paymentId, PaymentDTO.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.updateStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        return updatedPayment.toDTO();
    }

    public Map<String, Object> getPaymentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", paymentRepository.count());
        stats.put("completedPayments", paymentRepository.countByStatus(PaymentDTO.PaymentStatus.COMPLETED));
        stats.put("pendingPayments", paymentRepository.countByStatus(PaymentDTO.PaymentStatus.PENDING));
        stats.put("failedPayments", paymentRepository.countByStatus(PaymentDTO.PaymentStatus.FAILED));
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Double monthlyRevenue = paymentRepository.getTotalRevenueFromDate(startOfMonth);
        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
        
        return stats;
    }

    public void processExpiredPayments() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(30); // 30 minutes timeout
        List<Payment> expiredPayments = paymentRepository.findExpiredPendingPayments(expiredTime);
        
        for (Payment payment : expiredPayments) {
            payment.updateStatus(PaymentDTO.PaymentStatus.CANCELLED);
            payment.setFailureReason("Payment expired");
            paymentRepository.save(payment);
        }
    }

    public boolean isPaymentFromCustomer(Long paymentId, Long customerId) {
        return paymentRepository.findById(paymentId)
                .map(payment -> customerId.equals(payment.getCustomerId()))
                .orElse(false);
    }

    private Long extractCustomerIdFromContext() {
        // In a real implementation, this would extract the customer ID from the JWT token
        // For now, returning a placeholder
        return 1L;
    }
}
