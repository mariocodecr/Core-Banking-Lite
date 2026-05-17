package com.corebanking.modules.investment.repository;

import com.corebanking.modules.investment.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, String> {

    List<Instrument> findAllByActiveTrue();

    Optional<Instrument> findBySymbolAndActiveTrue(String symbol);
}
