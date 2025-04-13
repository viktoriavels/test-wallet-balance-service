package ru.vels.test.service.grouped;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
import ru.vels.test.service.BalanceServiceUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@Profile("balance-grouped")
@RequiredArgsConstructor
public class GroupedBalanceService implements BalanceService {

    private final GroupedBalanceWorkerService worker;
    private final BalanceRepository balanceRepository;

    @PostConstruct
    public void init() {
        log.info("balance-grouped service implementation using...");
    }

    @Override
    public void changeBalance(@Nonnull UUID walletId, @Nonnull ChangeBalanceOperation operation, @Nonnull BigDecimal amount) {
        try {
            worker.registerChangeBalanceWork(walletId, operation, amount).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NotEnoughBalanceException) {
                throw (NotEnoughBalanceException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public BigDecimal fetchBalance(@Nonnull UUID walletId) {
        try {
            WorkResult workResult = worker.registerGetBalanceWork(walletId).get();
            return workResult.getAmount();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof BalanceNotFoundException) {
                throw (BalanceNotFoundException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    @Retryable(retryFor = {
            OptimisticLockException.class,
            OptimisticLockingFailureException.class
    })
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Pair<BalanceCompletableFuture, WorkResult>> executeWorkGroup(List<BalanceCompletableFuture> futures) {
        UUID walletId = futures.get(0).getWorkItem().getWalletId();

        AtomicBoolean isNew = new AtomicBoolean(true);
        AtomicBoolean isChanged = new AtomicBoolean(false);
        BalanceEntity balance = findBalance(walletId, isNew);

        List<Pair<BalanceCompletableFuture, WorkResult>> result = new ArrayList<>(futures.size());
        for (BalanceCompletableFuture future : futures) {
            WorkResult workResult = executeWork(future, isNew, balance, isChanged);
            result.add(Pair.of(future, workResult));
        }

        if (isChanged.get()) {
            balanceRepository.save(balance);
        }

        return result;
    }

    private BalanceEntity findBalance(UUID walletId, AtomicBoolean isNew) {
        Optional<BalanceEntity> balanceOpt = balanceRepository.findById(walletId);
        if (balanceOpt.isPresent())  {
            isNew.set(false);
            return balanceOpt.get();
        } else {
            BalanceEntity newBalance = new BalanceEntity();
            newBalance.setWalletId(walletId);
            return newBalance;
        }
    }

    private WorkResult executeWork(BalanceCompletableFuture future, AtomicBoolean isNew, BalanceEntity balance, AtomicBoolean isChanged) {
        WorkItem workItem = future.getWorkItem();
        WorkResult workResult;
        if (workItem.getWorkType() == WorkType.GET_BALANCE) {
            workResult = executeGetBalanceImpl(isNew, balance);
        } else if (workItem.getWorkType() == WorkType.CHANGE_BALANCE) {
            workResult = executeChangeBalanceImpl(workItem, balance, isNew, isChanged);
        } else {
            throw new RuntimeException("unknown work type");
        }
        return workResult;
    }

    private WorkResult executeGetBalanceImpl(AtomicBoolean isNew, BalanceEntity balance) {
        if (isNew.get()) {
            return WorkResult.builder()
                    .throwable(new BalanceNotFoundException())
                    .build();
        } else {
            return WorkResult.builder()
                    .amount(BalanceServiceUtils.getAmountImpl(balance))
                    .build();
        }
    }

    private WorkResult executeChangeBalanceImpl(WorkItem workItem, BalanceEntity balance,
                                                AtomicBoolean isNew, AtomicBoolean isChanged) {
        ChangeBalanceOperation operation = workItem.getOperation();
        BigDecimal amount = workItem.getAmount();
        if (!BalanceServiceUtils.canPerformOperation(balance, operation, amount)) {
            return WorkResult.builder()
                    .throwable(new NotEnoughBalanceException())
                    .build();
        } else {
            BalanceServiceUtils.performOperation(balance, operation, amount);
            isNew.set(false);
            isChanged.set(true);
            return WorkResult.builder()
                    .build();
        }
    }
}
