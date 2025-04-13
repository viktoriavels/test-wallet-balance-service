package ru.vels.test.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "balances")
public class BalanceEntity {
    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "balance", nullable = false)
    private BigInteger balance;

    @Version
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BalanceEntity that = (BalanceEntity) o;
        return Objects.equals(walletId, that.walletId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(walletId);
    }
}

