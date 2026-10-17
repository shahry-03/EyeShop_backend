package com.eyeshop.product.service;

import com.eyeshop.product.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    /** Upload new image and attach to product. */
    ProductImageResponse uploadImage(Long productId, MultipartFile file);

    /** Delete image from product + Cloudinary. */
    void deleteImage(Long productId, Long imageId);

    /** Set as primary image (un-sets others). */
    ProductImageResponse setPrimary(Long productId, Long imageId);

    /** List all images of product. */
    List<ProductImageResponse> listImages(Long productId);
}