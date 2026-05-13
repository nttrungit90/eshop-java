package com.eshop.ordering.infrastructure.repositories;

import com.eshop.ordering.domain.aggregates.buyer.Buyer;
import com.eshop.ordering.domain.aggregates.buyer.BuyerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaBuyerRepository extends JpaRepository<Buyer, Long>, BuyerRepository {

    @Override
    Optional<Buyer> findByIdentityGuid(String identityGuid);
}
