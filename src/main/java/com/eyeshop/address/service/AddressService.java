package com.eyeshop.address.service;

import com.eyeshop.address.dto.request.AddressRequest;
import com.eyeshop.address.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressResponse create(UUID userId, AddressRequest request);

    List<AddressResponse> listForUser(UUID userId);

    AddressResponse getById(Long id, UUID userId);

    AddressResponse update(Long id, UUID userId, AddressRequest request);

    void delete(Long id, UUID userId);

    AddressResponse setDefault(Long id, UUID userId);
}