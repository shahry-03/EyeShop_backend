package com.eyeshop.order.controller;

import java.util.UUID;
import com.eyeshop.order.dto.request.OrderRequest;
import com.eyeshop.order.dto.response.OrderResponse;
import com.eyeshop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    //CUSTOMER Methods
    //Place Order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder( @Valid @RequestBody OrderRequest orderRequest){
        // Implementation will be done in service layer
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(orderRequest));
    }

    // Cancel Order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id){
        // Implementation will be done in service layer
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }



    //Admin Methods
    // Get Order By Id
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // Get Orders By User Id
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable UUID userId){
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

}
