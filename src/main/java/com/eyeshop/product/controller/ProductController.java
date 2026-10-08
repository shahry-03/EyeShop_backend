package com.eyeshop.product.controller;

import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // Admin methods
    // Add Product
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody @Valid ProductAddRequest productAddRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(productAddRequest));
    }

    // Update Product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody @Valid ProductUpdateRequest productUpdateRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, productUpdateRequest));
    }

    // Delete Product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Customer methods
    // Get All Products
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get Product By Id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Get Products By Category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }



}
