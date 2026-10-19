package com.eyeshop.product.controller;

import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.entity.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Helper method to check Admin role
    private boolean isAdmin(User user) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ═══════════════════════════════════════════════════════════
    //  ADMIN / SELLER — write operations
    // ═══════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')") // Dono allowed hain
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProductAddRequest request) {

        ProductResponse response = productService.addProduct(user.getId(), isAdmin(user), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse response = productService.updateProduct(id, user.getId(), isAdmin(user), request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        productService.deleteProduct(id, user.getId(), isAdmin(user));
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    //  SELLER DASHBOARD — read own products
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getMyProducts(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            productService.getSellerProducts(user.getId(), pageable)));
    }

    // ═══════════════════════════════════════════════════════════
    //  PUBLIC — read operations
    // ═══════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            productService.searchProducts(keyword, categoryId, minPrice, maxPrice, minRating, inStock, pageable)));
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
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
            productService.getProductsByCategoryId(categoryId, pageable)));
    }
}