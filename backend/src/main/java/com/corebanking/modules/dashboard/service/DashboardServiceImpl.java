package com.corebanking.modules.dashboard.service;

import com.corebanking.modules.account.entity.AccountType;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.repository.CustomerRepository;
import com.corebanking.modules.dashboard.dto.AccountTypeStatResponse;
import com.corebanking.modules.dashboard.dto.DailyTransferStatResponse;
import com.corebanking.modules.dashboard.dto.DashboardSummaryResponse;
import com.corebanking.modules.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.corebanking.config.CacheConfig.CACHE_DASHBOARD_SUMMARY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final AccountRepository  accountRepository;
    private final TransferRepository transferRepository;

    @Override
    @Cacheable(CACHE_DASHBOARD_SUMMARY)
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfToday   = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth   = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return DashboardSummaryResponse.builder()
                .totalCustomers(customerRepository.count())
                .activeCustomers(customerRepository.countByEstado(CustomerStatus.ACTIVO))
                .totalAccounts(accountRepository.countNonClosed())
                .totalBalanceUSD(accountRepository.sumSaldoByMoneda("USD"))
                .totalBalanceCRC(accountRepository.sumSaldoByMoneda("CRC"))
                .totalBalanceEUR(accountRepository.sumSaldoByMoneda("EUR"))
                .totalTransfersToday(transferRepository.countCompletedSince(startOfToday))
                .transferVolumeToday(transferRepository.sumVolumeSince(startOfToday))
                .totalTransfersThisMonth(transferRepository.countCompletedSince(startOfMonth))
                .transferVolumeThisMonth(transferRepository.sumVolumeSince(startOfMonth))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTypeStatResponse> getAccountTypeStats() {
        return accountRepository.findRawStatsByType().stream()
                .map(row -> new AccountTypeStatResponse(
                        (AccountType) row[0],
                        (Long) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyTransferStatResponse> getDailyTransferStats(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days - 1L).atStartOfDay();
        return transferRepository.findRawDailyStats(since).stream()
                .map(row -> new DailyTransferStatResponse(
                        ((java.sql.Date) row[0]).toLocalDate(),
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .toList();
    }
}
