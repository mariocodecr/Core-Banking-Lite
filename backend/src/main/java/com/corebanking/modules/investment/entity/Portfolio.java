package com.corebanking.modules.investment.entity;

import com.corebanking.audit.AuditableEntity;
import com.corebanking.modules.account.entity.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Investment portfolio linked 1:1 to a bank account.
 * Created lazily on the first BUY order.
 * The linked account must be denominated in USD.
 */
@Entity
@Table(
    name = "portfolios",
    uniqueConstraints = @UniqueConstraint(name = "uk_portfolios_account_id", columnNames = "account_id")
)
@Getter
@Setter
@NoArgsConstructor
public class Portfolio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Position> positions = new ArrayList<>();
}
