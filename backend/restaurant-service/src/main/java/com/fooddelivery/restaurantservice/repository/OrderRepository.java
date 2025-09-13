package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.common.dto.OrderDTO;
import com.fooddelivery.restaurantservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByRestaurantId(Long restaurantId);
    
    List<Order> findByStatus(OrderDTO.OrderStatus status);
    
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderDTO.OrderStatus status);
    
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findByRestaurantAndStatuses(@Param("restaurantId") Long restaurantId, @Param("statuses") List<OrderDTO.OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.createdAt >= :startDate ORDER BY o.createdAt DESC")
    List<Order> findByCustomerAndDateRange(@Param("customerId") Long customerId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.createdAt >= :startDate ORDER BY o.createdAt DESC")
    List<Order> findByRestaurantAndDateRange(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status")
    long countByRestaurantAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") OrderDTO.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    long countByCustomer(@Param("customerId") Long customerId);
}
