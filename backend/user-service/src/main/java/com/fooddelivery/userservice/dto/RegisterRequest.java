package com.fooddelivery.userservice.dto;

import com.fooddelivery.common.dto.UserDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank
    private String name;

    private String phone;

    private String address;

    @NotNull
    private UserDTO.UserRole role;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String name, UserDTO.UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public UserDTO.UserRole getRole() { return role; }
    public void setRole(UserDTO.UserRole role) { this.role = role; }
}
