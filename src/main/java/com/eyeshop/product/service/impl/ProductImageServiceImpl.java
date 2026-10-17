package com.eyeshop.product.service.impl;

import com.eyeshop.product.dto.response.ProductImageResponse;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.entity.ProductImage;
import com.eyeshop.product.exception.ImageUploadException;
import com.eyeshop.product.exception.ProductNotFoundException;
import com.eyeshop.product.mapper.ProductMapper;
import com.eyeshop.product.repository.ProductImageRepository;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.product.service.CloudinaryService;
import com.eyeshop.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;

    private static final int MAX_IMAGES_PER_PRODUCT = 8;

    // ═══════════════════════════════════════════════════════════
    //  UPLOAD
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        // Max limit check
        long existingCount = productImageRepository.countByProductId(productId);
        if (existingCount >= MAX_IMAGES_PER_PRODUCT) {
            throw new ImageUploadException(
                "Maximum " + MAX_IMAGES_PER_PRODUCT + " images allowed per product");
        }

        // Upload to Cloudinary
        CloudinaryService.UploadResult result = cloudinaryService.upload(file);

        // Is this the first image? Then primary
        boolean isFirst = existingCount == 0;

        ProductImage image = ProductImage.builder()
            .product(product)
            .url(result.url())
            .publicId(result.publicId())
            .isPrimary(isFirst)
            .displayOrder((int) existingCount)
            .build();

        ProductImage saved = productImageRepository.saveAndFlush(image);
        log.info("Image attached to product {}: imageId={}, primary={}",
            productId, saved.getId(), isFirst);

        return ProductMapper.toImageResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository
            .findByIdAndProductId(imageId, productId)
            .orElseThrow(() -> new ImageUploadException(
                "Image not found for this product: " + imageId));

        boolean wasPrimary = image.isPrimary();
        String publicId = image.getPublicId();

        // Delete from DB
        productImageRepository.delete(image);
        productImageRepository.flush();

        // Delete from Cloudinary (best-effort — don't fail transaction if CDN is down)
        try {
            cloudinaryService.delete(publicId);
        } catch (Exception e) {
            log.warn("Cloudinary deletion failed but DB record removed: publicId={}", publicId, e);
        }

        // Auto-promote next image as primary
        if (wasPrimary) {
            productImageRepository.findByProductIdOrderByIsPrimaryDescDisplayOrderAsc(productId)
                .stream()
                .findFirst()
                .ifPresent(next -> {
                    next.setPrimary(true);
                    productImageRepository.saveAndFlush(next);
                    log.info("Auto-promoted image {} as primary for product {}", next.getId(), productId);
                });
        }

        log.info("Image deleted from product {}: imageId={}", productId, imageId);
    }

    // ═══════════════════════════════════════════════════════════
    //  SET PRIMARY
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public ProductImageResponse setPrimary(Long productId, Long imageId) {
        ProductImage image = productImageRepository
            .findByIdAndProductId(imageId, productId)
            .orElseThrow(() -> new ImageUploadException(
                "Image not found for this product: " + imageId));

        if (image.isPrimary()) {
            return ProductMapper.toImageResponse(image);    // no-op
        }

        productImageRepository.clearPrimaryForProduct(productId);
        image.setPrimary(true);

        ProductImage saved = productImageRepository.saveAndFlush(image);
        log.info("Image {} set as primary for product {}", imageId, productId);

        return ProductMapper.toImageResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  LIST
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> listImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        return productImageRepository
            .findByProductIdOrderByIsPrimaryDescDisplayOrderAsc(productId)
            .stream()
            .map(ProductMapper::toImageResponse)
            .toList();
    }
}