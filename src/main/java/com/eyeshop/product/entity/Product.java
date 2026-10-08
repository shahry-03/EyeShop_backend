package com.eyeshop.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    @Column(nullable = false)
    @Positive(message = "Price must be greater than zero")
    private double price;

    @Column(nullable = false )
    @Min(value = 0, message = "Stock must be greater than or equal to zero")
    private int stock;

    @Column(nullable = false)
    @NotBlank(message = "Category cannot be empty")
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
