package com.corebanking.modules.exchangerate.repository;

import com.corebanking.modules.exchangerate.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
}
