package com.shop.repository;

import com.shop.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByUsername(String username);
    Optional<Seller> findByUsernameAndActive(String username, boolean active);
    boolean existsByUsername(String username);
}