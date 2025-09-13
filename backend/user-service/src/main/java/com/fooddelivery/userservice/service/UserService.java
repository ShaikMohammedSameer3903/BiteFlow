package com.fooddelivery.userservice.service;

import com.fooddelivery.common.dto.UserDTO;
import com.fooddelivery.userservice.entity.User;
import com.fooddelivery.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserDTO createUser(String email, String password, String name, UserDTO.UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = new User(email, passwordEncoder.encode(password), name, role);
        User savedUser = userRepository.save(user);
        return savedUser.toDTO();
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(User::toDTO);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(User::toDTO);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(User::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(UserDTO.UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(User::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getActiveUsers() {
        return userRepository.findByActiveTrue().stream()
                .map(User::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getActiveUsersByRole(UserDTO.UserRole role) {
        return userRepository.findActiveUsersByRole(role).stream()
                .map(User::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.updateFromDTO(userDTO);
        User updatedUser = userRepository.save(user);
        return updatedUser.toDTO();
    }

    public UserDTO updateUserProfile(String email, UserDTO userDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.updateFromDTO(userDTO);
        User updatedUser = userRepository.save(user);
        return updatedUser.toDTO();
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        user.setActive(true);
        userRepository.save(user);
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public long countUsersByRole(UserDTO.UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }

    public List<UserDTO> searchUsersByName(String name) {
        return userRepository.findActiveUsersByNameContaining(name).stream()
                .map(User::toDTO)
                .collect(Collectors.toList());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
