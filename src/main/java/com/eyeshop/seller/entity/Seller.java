package com.eyeshop.seller.entity;

import com.eyeshop.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String shopName;

    @Column(length = 500)
    private String shopDescription;

    @Column(nullable = false)
    @Builder.Default
    private Double commissionRate = 10.0; // 10% commission by default

    @Column(nullable = false)
    @Builder.Default
    private Double walletBalance = 0.0; // Payout balance

    @Column(nullable = false)
    @Builder.Default
    private boolean approved = false; // Admin approval required

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}