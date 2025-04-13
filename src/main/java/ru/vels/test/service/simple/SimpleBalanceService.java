package ru.vels.test.service.simple;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.vels.test.dto.ChangeBalanceOperation;
import ru.vels.test.entities.BalanceEntity;
import ru.vels.test.exceptions.BalanceNotFoundException;
import ru.vels.test.exceptions.NotEnoughBalanceException;
import ru.vels.test.repo.BalanceRepository;
import ru.vels.test.service.BalanceService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static ru.vels.test.service.BalanceServiceUtils.*;

@Slf4j
@Service
@Profile("!balance-grouped")
@RequiredArgsConstructor
public class SimpleBalanceService implements BalanceService {

    private final BalanceRepository balanceRepository;

    @PostConstruct
    public void init() {
        log.info("balance-grouped service implementation using...");
    }

    @Override
    @Retryable(retryFor = {
            OptimisticLockException.class,
            OptimisticLockingFailureException.class
    })
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void changeBalance(@Nonnull UUID walletId,
                              @Nonnull ChangeBalanceOperation operation,
                              @Nonnull BigDecimal amount) {

        Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
        BalanceEntity balance = balanceOpt.orElseGet(() -> {
            BalanceEntity newBalance = new BalanceEntity();
            newBalance.setWalletId(walletId);
            return newBalance;
        });

        if (!canPerformOperation(balance, operation, amount)) {
            throw new NotEnoughBalanceException();
        }

        performOperation(balance, operation, amount);

        balanceRepository.save(balance);
    }

    @Override
    @Retryable(retryFor = {
            OptimisticLockException.class,
            OptimisticLockingFailureException.class
    })
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public BigDecimal fetchBalance(@Nonnull UUID walletId) {
        Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
        if (balanceOpt.isEmpty()) {
            throw new BalanceNotFoundException();
        }
        return getAmountImpl(balanceOpt.get());
    }
}
