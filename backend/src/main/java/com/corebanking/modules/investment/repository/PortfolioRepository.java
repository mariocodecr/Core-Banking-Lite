package com.corebanking.modules.investment.repository;

import com.corebanking.modules.investment.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    Optional<Portfolio> findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);
}
