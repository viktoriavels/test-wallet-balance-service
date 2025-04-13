package ru.vels.test.service;

import jakarta.annotation.Nonnull;
import ru.vels.test.dto.ChangeBalanceOperation;
import ru.vels.test.entities.BalanceEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class BalanceServiceUtils {

    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public static boolean canPerformOperation(@Nonnull BalanceEntity balance,
                                              @Nonnull ChangeBalanceOperation operation,
                                              @Nonnull BigDecimal amount) {
        if (operation == ChangeBalanceOperation.DEPOSIT) {
            return true;
        } else {
            return getAmountImpl(balance).compareTo(amount) >= 0;
        }
    }

    public static void performOperation(@Nonnull BalanceEntity balance,
                                        @Nonnull ChangeBalanceOperation operation,
                                        @Nonnull BigDecimal amount) {
        BigInteger amountWithKopecks = amount.multiply(ONE_HUNDRED).toBigInteger();
        BigInteger currentAmount = balance.getBalance() != null ? balance.getBalance() : BigInteger.ZERO;
        if (operation == ChangeBalanceOperation.DEPOSIT) {
            balance.setBalance(currentAmount.add(amountWithKopecks));
        } else {
            balance.setBalance(currentAmount.subtract(amountWithKopecks));
        }
    }

    public static BigDecimal getAmountImpl(@Nonnull BalanceEntity balance) {
        BigInteger currentAmount = balance.getBalance() != null ? balance.getBalance() : BigInteger.ZERO;
        return new BigDecimal(currentAmount).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

}
