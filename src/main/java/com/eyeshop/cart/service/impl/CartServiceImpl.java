package com.eyeshop.cart.service.impl;

import com.eyeshop.auth.exception.ResourceNotFoundException;
import com.eyeshop.cart.dto.request.AddToCartRequest;
import com.eyeshop.cart.dto.request.UpdateCartItemRequest;
import com.eyeshop.cart.dto.response.CartResponse;
import com.eyeshop.cart.entity.Cart;
import com.eyeshop.cart.entity.CartItem;
import com.eyeshop.cart.exception.CartItemNotFoundException;
import com.eyeshop.cart.exception.CartNotFoundException;
import com.eyeshop.cart.mapper.CartMapper;
import com.eyeshop.cart.repository.CartRepository;
import com.eyeshop.cart.service.CartService;
import com.eyeshop.order.exception.InsufficientStockException;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Sanity limit per item — protects against accidental quantity=999999
     * or malicious DoS attempts. Adjust as business needs.
     */
    private static final int MAX_ITEM_QUANTITY = 999;

    // ═══════════════════════════════════════════════════════════════
    //  GET CART (auto-create)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public CartResponse getCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return CartMapper.toResponse(cart);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ADD ITEM (increment if exists)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public CartResponse addItem(UUID userId, AddToCartRequest request) {

        // 1. Load product (fails if doesn't exist)
        Product product = loadProduct(request.productId());

        // 2. Validate quantity limit
        validateQuantityLimit(request.quantity());

        // 3. Get or create cart
        Cart cart = getOrCreateCart(userId);

        // 4. Find existing item for this product (if any)
        CartItem existing = findItemByProduct(cart, product.getId());

        if (existing != null) {
            // ═══ Increment existing ═══
            int newQty = existing.getQuantity() + request.quantity();
            validateQuantityLimit(newQty);
            validateStock(product, newQty);

            existing.setQuantity(newQty);
            existing.setPrice(product.getPrice());       // refresh price snapshot
            existing.setProductName(product.getName());

            log.info("Cart item incremented: cartId={}, productId={}, qty={} (was {})",
                cart.getId(), product.getId(), newQty, existing.getQuantity() - request.quantity());
        } else {
            // ═══ Add new item ═══
            validateStock(product, request.quantity());

            CartItem item = CartItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(request.quantity())
                .build();

            cart.addItem(item);
            log.info("Cart item added: cartId={}, productId={}, qty={}",
                cart.getId(), product.getId(), request.quantity());
        }

        // 5. Touch updatedAt + persist
        cart.touch();
        Cart saved = cartRepository.saveAndFlush(cart);

        return CartMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════
    //  UPDATE ITEM QUANTITY
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public CartResponse updateItem(UUID userId, Long itemId, UpdateCartItemRequest request) {

        validateQuantityLimit(request.quantity());

        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemByIdOrThrow(cart, itemId);

        Product product = loadProduct(item.getProductId());
        validateStock(product, request.quantity());

        int oldQty = item.getQuantity();
        item.setQuantity(request.quantity());

        cart.touch();
        Cart saved = cartRepository.saveAndFlush(cart);

        log.info("Cart item qty updated: itemId={}, {} → {}", itemId, oldQty, request.quantity());
        return CartMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════
    //  REMOVE ITEM
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public CartResponse removeItem(UUID userId, Long itemId) {

        Cart cart = getCartOrThrow(userId);
        CartItem item = findItemByIdOrThrow(cart, itemId);

        cart.removeItem(item);
        cart.touch();

        Cart saved = cartRepository.saveAndFlush(cart);
        log.info("Cart item removed: itemId={}, productId={}", itemId, item.getProductId());
        return CartMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLEAR CART
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public CartResponse clearCart(UUID userId) {

        Cart cart = getCartOrThrow(userId);

        int itemCount = cart.getItems().size();
        cart.getItems().clear();
        cart.touch();

        Cart saved = cartRepository.saveAndFlush(cart);
        log.info("Cart cleared: cartId={}, itemsRemoved={}", saved.getId(), itemCount);
        return CartMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════
    //  EMPTY CART (internal — called by OrderService after checkout)
    // ═══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void emptyCartForUser(UUID userId) {
        cartRepository.findByUserId(userId).ifPresentOrElse(
            cart -> {
                cart.getItems().clear();
                cart.touch();
                cartRepository.saveAndFlush(cart);
                log.info("Cart emptied after order: cartId={}, userId={}", cart.getId(), userId);
            },
            () -> log.warn("Empty cart called for user {} but no cart exists", userId)
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** Get or create a cart for the user (idempotent). */
    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = Cart.builder()
                    .userId(userId)
                    .build();
                Cart saved = cartRepository.save(newCart);
                log.info("Cart auto-created: cartId={}, userId={}", saved.getId(), userId);
                return saved;
            });
    }

    /** Get existing cart or throw 404. */
    private Cart getCartOrThrow(UUID userId) {
        return cartRepository.findByUserId(userId)
            .orElseThrow(() -> new CartNotFoundException(
                "Cart not found for user: " + userId));
    }

    /** Load product or throw 404. */
    private Product loadProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found: " + productId));
    }

    /** Find cart item by ID (scoped to this cart) or throw 404. */
    private CartItem findItemByIdOrThrow(Cart cart, Long itemId) {
        return cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new CartItemNotFoundException(
                "Cart item not found: " + itemId));
    }

    /** Find cart item by product ID (returns null if not present). */
    private CartItem findItemByProduct(Cart cart, Long productId) {
        return cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }

    /** Validate that a single item's quantity doesn't exceed sanity limit. */
    private void validateQuantityLimit(int quantity) {
        if (quantity > MAX_ITEM_QUANTITY) {
            throw new IllegalArgumentException(
                String.format("Quantity exceeds maximum allowed per item: %d (requested %d)",
                    MAX_ITEM_QUANTITY, quantity));
        }
    }

    /** Verify product has enough stock for requested quantity. */
    private void validateStock(Product product, int requestedQty) {
        if (product.getStock() < requestedQty) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for '%s'. Available: %d, requested: %d",
                    product.getName(), product.getStock(), requestedQty));
        }
    }
}