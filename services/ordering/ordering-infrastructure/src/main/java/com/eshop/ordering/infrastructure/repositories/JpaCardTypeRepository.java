package com.eshop.ordering.infrastructure.repositories;

import com.eshop.ordering.domain.aggregates.buyer.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCardTypeRepository extends JpaRepository<CardType, Long> {
}
