package com.eyeshop.address.dto.request;

import com.eyeshop.address.entity.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Address create/update request.
 * Same DTO for POST (create) and PUT (update).
 */
public record AddressRequest(

    AddressLabel label,                                    // optional, default HOME

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    String fullName,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+0-9\\-\\s()]{7,20}$", message = "Invalid phone number")
    String phone,

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 300, message = "Address line 1 must not exceed 300 characters")
    String addressLine1,

    @Size(max = 300, message = "Address line 2 must not exceed 300 characters")
    String addressLine2,

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,

    @Size(max = 100, message = "State must not exceed 100 characters")
    String state,

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    String postalCode,

    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country,

    Boolean isDefault                                     // optional
) {}