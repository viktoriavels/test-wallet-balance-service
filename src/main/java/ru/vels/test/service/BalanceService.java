package ru.vels.test.service;

import jakarta.annotation.Nonnull;
import ru.vels.test.dto.ChangeBalanceOperation;

import java.math.BigDecimal;
import java.util.UUID;

public interface BalanceService {

    void changeBalance(@Nonnull UUID walletId,
                       @Nonnull ChangeBalanceOperation operation,
                       @Nonnull BigDecimal amount);

    BigDecimal fetchBalance(@Nonnull UUID walletId);
}
