package com.fooddelivery.paymentservice.repository;

import com.fooddelivery.common.dto.PaymentDTO;
import com.fooddelivery.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(Long orderId);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    List<Payment> findByCustomerId(Long customerId);
    
    List<Payment> findByStatus(PaymentDTO.PaymentStatus status);
    
    List<Payment> findByPaymentMethod(PaymentDTO.PaymentMethod paymentMethod);
    
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.createdAt >= :startDate ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerAndDateRange(@Param("customerId") Long customerId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate")
    List<Payment> findByStatusAndDateRange(@Param("status") PaymentDTO.PaymentStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentDTO.PaymentStatus status);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customerId = :customerId AND p.status = 'COMPLETED'")
    long countSuccessfulPaymentsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :startDate")
    Double getTotalRevenueFromDate(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :expiredTime")
    List<Payment> findExpiredPendingPayments(@Param("expiredTime") LocalDateTime expiredTime);
}
