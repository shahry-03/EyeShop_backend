package com.eyeshop.product.service;

import com.eyeshop.product.dto.request.ProductAddRequest;
import com.eyeshop.product.dto.request.ProductUpdateRequest;
import com.eyeshop.product.dto.response.ProductResponse;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ADD Product
    public ProductResponse addProduct(ProductAddRequest productAddRequest) {
        if (productRepository.existsByName(productAddRequest.getName())) {
            throw new IllegalArgumentException("Product with this name already exists: " + productAddRequest.getName());
        }
        Product product = Product.builder()
                .name(productAddRequest.getName())
                .description(productAddRequest.getDescription())
                .price(productAddRequest.getPrice())
                .stock(productAddRequest.getStock())
                .category(productAddRequest.getCategory())
                .build();
        return ProductResponse.fromEntity(productRepository.save(product));
    }

    // Get all products
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    // Get product by id
    public ProductResponse getProductById(Long id) {
        return ProductResponse.fromEntity(productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)));
    }

    // Get products by category
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    // Update product
    public ProductResponse updateProduct(Long id, ProductUpdateRequest productUpdateRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existingProduct.setName(productUpdateRequest.getName());
        existingProduct.setDescription(productUpdateRequest.getDescription());
        existingProduct.setPrice(productUpdateRequest.getPrice());
        existingProduct.setCategory(productUpdateRequest.getCategory());
        return ProductResponse.fromEntity(productRepository.save(existingProduct));
    }

    // Delete product
    public void deleteProduct(Long id) {
        // Check if product exists before deleting
        productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.deleteById(id);
    }
}
