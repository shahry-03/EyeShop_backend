package com.eyeshop.order.service;

import java.util.UUID;
import com.eyeshop.order.dto.request.OrderItemRequest;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.entity.Order;
import com.eyeshop.order.entity.OrderItem;
import com.eyeshop.order.entity.Status;
import com.eyeshop.order.repository.OrderRepository;
import com.eyeshop.product.entity.Product;
import com.eyeshop.product.repository.ProductRepository;
import com.eyeshop.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // --- Place the Order
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        // Step1: user exists check
        userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + orderRequest.getUserId()));

        double totalPrice = 0;

        // List of Order Items
        List<OrderItem> orderItems = new ArrayList<>();

        // Step2: check for each order item
        for(OrderItemRequest itemRequest: orderRequest.getItemsList()){
            // product exists check
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: "
                            + itemRequest.getProductId()));

            // stock check
            if(product.getStock() < itemRequest.getQuantity()){
                throw new RuntimeException("Insufficient stock for product: "
                        + product.getName() + ". Available stock: " + product.getStock());
            }

            // Calculate the price
            double itemPrice = product.getPrice() * itemRequest.getQuantity();;

            // add in total price
            totalPrice += itemPrice;

            // Reduce the stock
            product.setStock(product.getStock() - itemRequest.getQuantity());

            // build the Order Item
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemPrice)
                    .build();

            orderItems.add(orderItem);

        }


        // Step 5: Build the Order
        Order order = Order.builder()
                .userId(orderRequest.getUserId())
                .totalPrice(totalPrice)
                .status(Status.PLACED)
                .items(orderItems)
                .build();

        //Step4: Link each item with Order
        for (OrderItem orderItem : orderItems){
            orderItem.setOrder(order);
        }

        // save the order
        return OrderResponse.fromEntity(orderRepository.save(order));

    }

    // ---Get Order By Id
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return OrderResponse.fromEntity(order);
    }

    // --- Get Orders by userId
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    // Cancel the order
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        // First of all we find the order which we want to cancel
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        //Already Cancelled check
        if (order.getStatus() == Status.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }
        // Restore the Stock
        for (OrderItem orderItem : order.getItems()) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: "+ orderItem.getProductId()));
            // Restore the stock of the product in each order item
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        //Update the status
        order.setStatus(Status.CANCELLED);
        return OrderResponse.fromEntity(orderRepository.save(order));
    }
}
