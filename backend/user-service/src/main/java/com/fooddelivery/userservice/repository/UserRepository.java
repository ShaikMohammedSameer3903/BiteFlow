package com.fooddelivery.userservice.repository;

import com.fooddelivery.common.dto.UserDTO;
import com.fooddelivery.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserDTO.UserRole role);
    
    List<User> findByActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") UserDTO.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.active = true")
    List<User> findActiveUsersByNameContaining(@Param("name") String name);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countActiveUsersByRole(@Param("role") UserDTO.UserRole role);
}
