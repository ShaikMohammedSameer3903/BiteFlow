package com.fooddelivery.deliveryservice.repository;

import com.fooddelivery.common.dto.DeliveryDTO;
import com.fooddelivery.deliveryservice.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
    Optional<Delivery> findByOrderId(Long orderId);
    
    Optional<Delivery> findByTrackingCode(String trackingCode);
    
    List<Delivery> findByDeliveryPersonId(Long deliveryPersonId);
    
    List<Delivery> findByStatus(DeliveryDTO.DeliveryStatus status);
    
    List<Delivery> findByDeliveryPersonIdAndStatus(Long deliveryPersonId, DeliveryDTO.DeliveryStatus status);
    
    List<Delivery> findByDeliveryPersonIdOrderByCreatedAtDesc(Long deliveryPersonId);
    
    @Query("SELECT d FROM Delivery d WHERE d.status IN :statuses ORDER BY d.createdAt ASC")
    List<Delivery> findByStatusIn(@Param("statuses") List<DeliveryDTO.DeliveryStatus> statuses);
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveryPersonId = :deliveryPersonId AND d.status IN :statuses ORDER BY d.createdAt ASC")
    List<Delivery> findByDeliveryPersonAndStatuses(@Param("deliveryPersonId") Long deliveryPersonId, @Param("statuses") List<DeliveryDTO.DeliveryStatus> statuses);
    
    @Query("SELECT d FROM Delivery d WHERE d.status = 'PENDING' ORDER BY d.createdAt ASC")
    List<Delivery> findPendingDeliveries();
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveryPersonId = :deliveryPersonId AND d.createdAt >= :startDate ORDER BY d.createdAt DESC")
    List<Delivery> findByDeliveryPersonAndDateRange(@Param("deliveryPersonId") Long deliveryPersonId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.deliveryPersonId = :deliveryPersonId AND d.status = :status")
    long countByDeliveryPersonAndStatus(@Param("deliveryPersonId") Long deliveryPersonId, @Param("status") DeliveryDTO.DeliveryStatus status);
    
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.status = :status")
    long countByStatus(@Param("status") DeliveryDTO.DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveryPersonId IS NULL AND d.status = 'PENDING' ORDER BY d.createdAt ASC")
    List<Delivery> findUnassignedDeliveries();
}
