package com.fooddelivery.deliveryservice.service;

import com.fooddelivery.common.dto.DeliveryDTO;
import com.fooddelivery.deliveryservice.entity.Delivery;
import com.fooddelivery.deliveryservice.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    public DeliveryDTO createDelivery(DeliveryDTO deliveryDTO) {
        Delivery delivery = new Delivery(
            deliveryDTO.getOrderId(),
            deliveryDTO.getPickupAddress(),
            deliveryDTO.getDeliveryAddress()
        );
        
        delivery.setDeliveryNotes(deliveryDTO.getDeliveryNotes());
        delivery.setEstimatedDeliveryTime(deliveryDTO.getEstimatedDeliveryTime());
        
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return savedDelivery.toDTO();
    }

    public Optional<DeliveryDTO> getDeliveryById(Long id) {
        return deliveryRepository.findById(id).map(Delivery::toDTO);
    }

    public Optional<DeliveryDTO> getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId).map(Delivery::toDTO);
    }

    public Optional<DeliveryDTO> getDeliveryByTrackingCode(String trackingCode) {
        return deliveryRepository.findByTrackingCode(trackingCode).map(Delivery::toDTO);
    }

    public List<DeliveryDTO> getDeliveriesByPerson(Long deliveryPersonId) {
        return deliveryRepository.findByDeliveryPersonIdOrderByCreatedAtDesc(deliveryPersonId).stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getDeliveriesByStatus(DeliveryDTO.DeliveryStatus status) {
        return deliveryRepository.findByStatus(status).stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getActiveDeliveriesByPerson(Long deliveryPersonId) {
        List<DeliveryDTO.DeliveryStatus> activeStatuses = Arrays.asList(
            DeliveryDTO.DeliveryStatus.ASSIGNED,
            DeliveryDTO.DeliveryStatus.PICKED_UP,
            DeliveryDTO.DeliveryStatus.OUT_FOR_DELIVERY
        );
        
        return deliveryRepository.findByDeliveryPersonAndStatuses(deliveryPersonId, activeStatuses).stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getPendingDeliveries() {
        return deliveryRepository.findPendingDeliveries().stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getUnassignedDeliveries() {
        return deliveryRepository.findUnassignedDeliveries().stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryDTO> getRecentDeliveriesByPerson(Long deliveryPersonId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return deliveryRepository.findByDeliveryPersonAndDateRange(deliveryPersonId, startDate).stream()
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }

    public DeliveryDTO assignDeliveryPerson(Long deliveryId, Long deliveryPersonId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        if (delivery.getStatus() != DeliveryDTO.DeliveryStatus.PENDING) {
            throw new RuntimeException("Delivery is not in pending status");
        }

        delivery.setDeliveryPersonId(deliveryPersonId);
        delivery.updateStatus(DeliveryDTO.DeliveryStatus.ASSIGNED);
        
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return updatedDelivery.toDTO();
    }

    public DeliveryDTO updateDeliveryStatus(Long deliveryId, DeliveryDTO.DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.updateStatus(status);
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return updatedDelivery.toDTO();
    }

    public DeliveryDTO updateLocation(Long deliveryId, Double latitude, Double longitude) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.updateLocation(latitude, longitude);
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return updatedDelivery.toDTO();
    }

    public DeliveryDTO updateEstimatedTime(Long deliveryId, LocalDateTime estimatedTime) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setEstimatedDeliveryTime(estimatedTime);
        delivery.setUpdatedAt(LocalDateTime.now());
        
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return updatedDelivery.toDTO();
    }

    public DeliveryDTO addDeliveryNotes(Long deliveryId, String notes) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setDeliveryNotes(notes);
        delivery.setUpdatedAt(LocalDateTime.now());
        
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return updatedDelivery.toDTO();
    }

    public void cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        if (delivery.getStatus() == DeliveryDTO.DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        delivery.updateStatus(DeliveryDTO.DeliveryStatus.CANCELLED);
        deliveryRepository.save(delivery);
    }

    public long getDeliveryCountByPersonAndStatus(Long deliveryPersonId, DeliveryDTO.DeliveryStatus status) {
        return deliveryRepository.countByDeliveryPersonAndStatus(deliveryPersonId, status);
    }

    public long getDeliveryCountByStatus(DeliveryDTO.DeliveryStatus status) {
        return deliveryRepository.countByStatus(status);
    }

    public boolean isDeliveryAssignedToPerson(Long deliveryId, Long deliveryPersonId) {
        return deliveryRepository.findById(deliveryId)
                .map(delivery -> deliveryPersonId.equals(delivery.getDeliveryPersonId()))
                .orElse(false);
    }

    public List<DeliveryDTO> findOptimalDeliveries(Long deliveryPersonId, Double currentLat, Double currentLng, int maxDeliveries) {
        // Simple implementation - in real scenario, this would use AI service for route optimization
        List<Delivery> unassigned = deliveryRepository.findUnassignedDeliveries();
        
        return unassigned.stream()
                .limit(maxDeliveries)
                .map(Delivery::toDTO)
                .collect(Collectors.toList());
    }
}
