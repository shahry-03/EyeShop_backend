package com.eyeshop.seller.service.impl;

import com.eyeshop.auth.entity.Role;
import com.eyeshop.auth.entity.User;
import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.auth.repositories.RoleRepository;
import com.eyeshop.auth.repositories.UserRepository;
import com.eyeshop.seller.entity.Seller;
import com.eyeshop.seller.repository.SellerRepository;
import com.eyeshop.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Seller applyForSeller(UUID userId, String shopName, String description) {
        if (sellerRepository.existsByUserId(userId)) {
            throw new RuntimeException("You have already applied for a seller account.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Seller seller = Seller.builder()
            .user(user)
            .shopName(shopName)
            .shopDescription(description)
            .approved(false)
            .build();

        return sellerRepository.save(seller);
    }

    @Override
    @Transactional
    public Seller approveSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (seller.isApproved()) {
            return seller; // Already approved
        }

        seller.setApproved(true);
        
        // Assign SELLER role to user
        User user = seller.getUser();
        Role sellerRole = roleRepository.findByRoleName("SELLER")
            .orElseThrow(() -> new ResourceNotFoundException("SELLER role not found in DB. Please insert it in roles table."));
        
        user.getRoles().add(sellerRole);
        userRepository.save(user);

        return sellerRepository.save(seller);
    }

    @Override
    public Seller getSellerByUserId(UUID userId) {
        return sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
    }

    @Override
    public Double getWalletBalance(UUID userId) {
        return sellerRepository.findByUserId(userId)
            .map(Seller::getWalletBalance)
            .orElse(0.0);
    }
}