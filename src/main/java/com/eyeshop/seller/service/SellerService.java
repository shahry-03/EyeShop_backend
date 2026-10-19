package com.eyeshop.seller.service;

import com.eyeshop.seller.entity.Seller;
import java.util.UUID;

public interface SellerService {
    Seller applyForSeller(UUID userId, String shopName, String description);
    Seller approveSeller(Long sellerId);
    Seller getSellerByUserId(UUID userId);
    
    
    Double getWalletBalance(UUID userId); 
}