package org.example.entity.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCompanyDTO(
    @NotNull(message = "Company ID cannot be null")
    UUID companyId,
    
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,
    
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]*$", message = "Phone number contains invalid characters")
    String phoneNumber,
    
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    String name,
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address,
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country
) {}
