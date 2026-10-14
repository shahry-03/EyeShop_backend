package com.eyeshop.address.repository;

import com.eyeshop.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /** All addresses of a user, default first, then by createdAt desc. */
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    /** Find specific address owned by user (ownership enforced). */
    Optional<Address> findByIdAndUserId(Long id, UUID userId);

    /** Current default address of user (if any). */
    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    /** Count how many addresses a user has. */
    long countByUserId(UUID userId);

    /**
     * Unset default flag for all addresses of a user.
     * Used before setting a new default.
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId AND a.isDefault = true")
    int clearDefaultForUser(@Param("userId") UUID userId);
}