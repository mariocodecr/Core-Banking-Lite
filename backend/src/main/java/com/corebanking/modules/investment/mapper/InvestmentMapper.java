package com.corebanking.modules.investment.mapper;

import com.corebanking.modules.investment.dto.InstrumentResponse;
import com.corebanking.modules.investment.dto.InvestmentOrderResponse;
import com.corebanking.modules.investment.entity.Instrument;
import com.corebanking.modules.investment.entity.InvestmentOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvestmentMapper {

    InstrumentResponse toInstrumentResponse(Instrument instrument);

    List<InstrumentResponse> toInstrumentResponseList(List<Instrument> instruments);

    @Mapping(target = "portfolioId",    source = "portfolio.id")
    @Mapping(target = "symbol",         source = "instrument.symbol")
    @Mapping(target = "instrumentName", source = "instrument.name")
    InvestmentOrderResponse toOrderResponse(InvestmentOrder order);

    List<InvestmentOrderResponse> toOrderResponseList(List<InvestmentOrder> orders);
}
