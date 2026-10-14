package com.eyeshop.address.service.impl;

import com.eyeshop.address.dto.request.AddressRequest;
import com.eyeshop.address.dto.response.AddressResponse;
import com.eyeshop.address.entity.Address;
import com.eyeshop.address.entity.AddressLabel;
import com.eyeshop.address.exception.AddressNotFoundException;
import com.eyeshop.address.mapper.AddressMapper;
import com.eyeshop.address.repository.AddressRepository;
import com.eyeshop.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    // ═══════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {

        boolean isFirstAddress = addressRepository.countByUserId(userId) == 0;

        // Determine default flag:
        //  - If user has no addresses → auto default
        //  - Else → use request.isDefault() (or false)
        boolean shouldBeDefault = isFirstAddress
            || Boolean.TRUE.equals(request.isDefault());

        // If becoming default, clear existing default
        if (shouldBeDefault && !isFirstAddress) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
            .userId(userId)
            .label(request.label() != null ? request.label() : AddressLabel.HOME)
            .fullName(request.fullName())
            .phone(request.phone())
            .addressLine1(request.addressLine1())
            .addressLine2(request.addressLine2())
            .city(request.city())
            .state(request.state())
            .postalCode(request.postalCode())
            .country(request.country() != null ? request.country() : "Pakistan")
            .isDefault(shouldBeDefault)
            .build();

        Address saved = addressRepository.saveAndFlush(address);
        log.info("Address created: id={}, userId={}, default={}", saved.getId(), userId, shouldBeDefault);

        return AddressMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  LIST
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listForUser(UUID userId) {
        return AddressMapper.toResponseList(
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId));
    }

    // ═══════════════════════════════════════════════════════════
    //  GET BY ID
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(Long id, UUID userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AddressNotFoundException("Address not found: " + id));
        return AddressMapper.toResponse(address);
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public AddressResponse update(Long id, UUID userId, AddressRequest request) {

        Address address = addressRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AddressNotFoundException("Address not found: " + id));

        // Update fields
        if (request.label() != null) address.setLabel(request.label());
        address.setFullName(request.fullName());
        address.setPhone(request.phone());
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        if (request.country() != null) address.setCountry(request.country());

        // Handle default flag change
        if (Boolean.TRUE.equals(request.isDefault()) && !address.isDefault()) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        }

        Address updated = addressRepository.saveAndFlush(address);

        log.info("Address updated: id={}, userId={}", id, userId);
        return AddressMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void delete(Long id, UUID userId) {

        Address address = addressRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AddressNotFoundException("Address not found: " + id));

        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);

        // If default was deleted, promote the most recent remaining address
        if (wasDefault) {
            addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .findFirst()
                .ifPresent(next -> {
                    next.setDefault(true);
                    addressRepository.save(next);
                    log.info("Auto-promoted address {} as default for user {}",
                        next.getId(), userId);
                });
        }

        log.info("Address deleted: id={}, userId={}", id, userId);
    }

    // ═══════════════════════════════════════════════════════════
    //  SET DEFAULT
    // ═══════════════════════════════════════════════════════════
    @Override
    @Transactional
    public AddressResponse setDefault(Long id, UUID userId) {

        Address address = addressRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new AddressNotFoundException("Address not found: " + id));

        if (address.isDefault()) {
            return AddressMapper.toResponse(address);   // already default
        }

        addressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        Address updated = addressRepository.saveAndFlush(address);

        log.info("Address {} set as default for user {}", id, userId);
        return AddressMapper.toResponse(updated);
    }
}