package com.eyeshop.address.mapper;

import com.eyeshop.address.dto.response.AddressResponse;
import com.eyeshop.address.entity.Address;

import java.util.Collections;
import java.util.List;

public final class AddressMapper {

    private AddressMapper() {}

    public static AddressResponse toResponse(Address address) {
        if (address == null) return null;
        return new AddressResponse(
            address.getId(),
            address.getLabel(),
            address.getFullName(),
            address.getPhone(),
            address.getAddressLine1(),
            address.getAddressLine2(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry(),
            address.isDefault(),
            address.getCreatedAt(),
            address.getUpdatedAt()
        );
    }

    public static List<AddressResponse> toResponseList(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) return Collections.emptyList();
        return addresses.stream().map(AddressMapper::toResponse).toList();
    }
}