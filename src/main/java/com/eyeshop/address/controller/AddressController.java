package com.eyeshop.address.controller;

import com.eyeshop.address.dto.request.AddressRequest;
import com.eyeshop.address.dto.response.AddressResponse;
import com.eyeshop.address.service.AddressService;
import com.eyeshop.auth.dto.response.ApiResponse;
import com.eyeshop.auth.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AddressController {

    private final AddressService addressService;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response = addressService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Address created", response));
    }

    // ═══════════════════════════════════════════════════════════
    //  LIST
    // ═══════════════════════════════════════════════════════════
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> list(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(
            addressService.listForUser(user.getId())));
    }

    // ═══════════════════════════════════════════════════════════
    //  GET BY ID
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success(
            addressService.getById(id, user.getId())));
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Address updated",
            addressService.update(id, user.getId(), request)));
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        addressService.delete(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Address deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    //  SET DEFAULT
    // ═══════════════════════════════════════════════════════════
    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(ApiResponse.success("Address set as default",
            addressService.setDefault(id, user.getId())));
    }
}