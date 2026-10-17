package com.eyeshop.product.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.product.dto.response.ProductImageResponse;
import com.eyeshop.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    // ═══════════════════════════════════════════════════════════
    //  PUBLIC — list images (any authenticated user)
    // ═══════════════════════════════════════════════════════════
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> list(
            @PathVariable Long productId) {

        return ResponseEntity.ok(ApiResponse.success(
            productImageService.listImages(productId)));
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — upload image (multipart/form-data)
    // ═══════════════════════════════════════════════════════════
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        ProductImageResponse response = productImageService.uploadImage(productId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Image uploaded", response));
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — delete image
    // ═══════════════════════════════════════════════════════════
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — set as primary
    // ═══════════════════════════════════════════════════════════
    @PatchMapping("/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> setPrimary(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        return ResponseEntity.ok(ApiResponse.success("Primary image updated",
            productImageService.setPrimary(productId, imageId)));
    }
}