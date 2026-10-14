package com.eyeshop.address.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "addresses_user_id_idx", columnList = "user_id"),
    @Index(name = "addresses_user_default_idx", columnList = "user_id, is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of this address. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AddressLabel label = AddressLabel.HOME;

    @Column(name = "full_name", nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String fullName;

    @Column(nullable = false, length = 20)
    @NotBlank
    @Pattern(regexp = "^[+0-9\\-\\s()]{7,20}$", message = "Invalid phone number")
    private String phone;

    @Column(name = "address_line1", nullable = false, length = 300)
    @NotBlank
    @Size(max = 300)
    private String addressLine1;

    @Column(name = "address_line2", length = 300)
    @Size(max = 300)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String city;

    @Column(length = 100)
    @Size(max = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    @Size(max = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    @Builder.Default
    private String country = "Pakistan";

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}