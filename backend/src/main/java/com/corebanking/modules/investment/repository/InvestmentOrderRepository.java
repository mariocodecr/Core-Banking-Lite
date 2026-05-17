package com.corebanking.modules.investment.repository;

import com.corebanking.modules.investment.entity.InvestmentOrder;
import com.corebanking.modules.investment.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, UUID> {

    Page<InvestmentOrder> findByPortfolioIdOrderByFechaOrdenDesc(UUID portfolioId, Pageable pageable);

    /** Used by the scheduler to pick up all pending mutual fund orders. */
    @Query("SELECT o FROM InvestmentOrder o JOIN FETCH o.portfolio p JOIN FETCH p.account JOIN FETCH o.instrument WHERE o.estado = :estado")
    List<InvestmentOrder> findByEstadoWithDetails(OrderStatus estado);
}
