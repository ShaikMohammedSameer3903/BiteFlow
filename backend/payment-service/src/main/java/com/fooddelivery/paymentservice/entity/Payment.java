package com.fooddelivery.paymentservice.entity;

import com.fooddelivery.common.dto.PaymentDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentDTO.PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentDTO.PaymentStatus status;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    // Constructors
    public Payment() {}

    public Payment(Long orderId, BigDecimal amount, PaymentDTO.PaymentMethod paymentMethod, Long customerId) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.customerId = customerId;
        this.status = PaymentDTO.PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Convert to DTO
    public PaymentDTO toDTO() {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(this.id);
        dto.setOrderId(this.orderId);
        dto.setAmount(this.amount);
        dto.setPaymentMethod(this.paymentMethod);
        dto.setStatus(this.status);
        dto.setTransactionId(this.transactionId);
        dto.setGatewayResponse(this.gatewayResponse);
        dto.setCreatedAt(this.createdAt);
        dto.setProcessedAt(this.processedAt);
        dto.setFailureReason(this.failureReason);
        dto.setRefundAmount(this.refundAmount);
        dto.setRefundedAt(this.refundedAt);
        return dto;
    }

    // Update status
    public void updateStatus(PaymentDTO.PaymentStatus newStatus) {
        this.status = newStatus;
        if (newStatus == PaymentDTO.PaymentStatus.COMPLETED || 
            newStatus == PaymentDTO.PaymentStatus.FAILED) {
            this.processedAt = LocalDateTime.now();
        }
    }

    // Process refund
    public void processRefund(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
        this.refundedAt = LocalDateTime.now();
        
        if (refundAmount.compareTo(this.amount) == 0) {
            this.status = PaymentDTO.PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentDTO.PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentDTO.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentDTO.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentDTO.PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentDTO.PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }
}
