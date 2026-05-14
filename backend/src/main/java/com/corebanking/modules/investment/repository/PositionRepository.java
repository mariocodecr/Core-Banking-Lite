package com.corebanking.modules.investment.repository;

import com.corebanking.modules.investment.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    List<Position> findByPortfolioId(UUID portfolioId);

    Optional<Position> findByPortfolioIdAndInstrumentSymbol(UUID portfolioId, String symbol);

    @Query("SELECT p FROM Position p JOIN FETCH p.instrument")
    List<Position> findAllWithInstrument();
}
