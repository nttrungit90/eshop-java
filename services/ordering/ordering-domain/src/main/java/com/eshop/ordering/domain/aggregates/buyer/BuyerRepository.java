package com.eshop.ordering.domain.aggregates.buyer;

import java.util.Optional;

public interface BuyerRepository {

    Buyer save(Buyer buyer);

    Optional<Buyer> findById(Long id);

    Optional<Buyer> findByIdentityGuid(String identityGuid);
}
