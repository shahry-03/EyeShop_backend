package com.eyeshop.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.ArrayList;
import java.util.List;
import com.eyeshop.category.entity.Category;

import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "products_category_idx", columnList = "category_id"), 
    @Index(name = "products_name_idx", columnList = "name")
})
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

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @Positive(message = "Price must be greater than zero")
    private double price;

    @Column(nullable = false)
    @Min(value = 0, message = "Stock must be greater than or equal to zero")
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optimistic locking — prevents concurrent stock updates from causing
     * race conditions. If two requests try to update the same product at the
     * same time, one will fail with ObjectOptimisticLockingFailureException
     * (handled by GlobalExceptionHandler → 409 Conflict).
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("isPrimary DESC, displayOrder ASC, id ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();
}