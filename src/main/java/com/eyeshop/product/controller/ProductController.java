package com.eyeshop.product.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.common.dto.response.PageResponse;
import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ═══════════════════════════════════════════════════════════
    //  ADMIN — write operations
    // ═══════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductAddRequest request) {

        ProductResponse response = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    //  PUBLIC (authenticated) — read operations
    // ═══════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            productService.getAllProducts(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/category/{categoryId}") 
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId, 
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            productService.getProductsByCategoryId(categoryId, pageable))); 
    }
}