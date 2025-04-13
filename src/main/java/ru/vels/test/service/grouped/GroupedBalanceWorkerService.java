package ru.vels.test.service.grouped;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import ru.vels.test.dto.ChangeBalanceOperation;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Profile("balance-grouped")
@RequiredArgsConstructor
public class GroupedBalanceWorkerService {

    private final LinkedMultiValueMap<UUID, BalanceCompletableFuture> workMap = new LinkedMultiValueMap<>();

    public BalanceCompletableFuture registerChangeBalanceWork(UUID walletId, ChangeBalanceOperation operation, BigDecimal amount) {
        synchronized (workMap) {
            BalanceCompletableFuture future = new BalanceCompletableFuture(WorkItem.builder()
                    .workType(WorkType.CHANGE_BALANCE)
                    .walletId(walletId)
                    .operation(operation)
                    .amount(amount)
                    .build());
            workMap.add(walletId, future);
            log.info("notification about new work");
            workMap.notifyAll();
            return future;
        }
    }

    public BalanceCompletableFuture registerGetBalanceWork(UUID walletId) {
        synchronized (workMap) {
            BalanceCompletableFuture future = new BalanceCompletableFuture(WorkItem.builder()
                    .workType(WorkType.GET_BALANCE)
                    .walletId(walletId)
                    .build());
            workMap.add(walletId, future);
            log.info("notification about new work");
            workMap.notifyAll();
            return future;
        }
    }

    @SneakyThrows
    public List<BalanceCompletableFuture> waitAndReceiveWork() {
        synchronized (workMap) {
            if (workMap.isEmpty()) {
                log.info("worker will wait for new work");
                workMap.wait();
                return Collections.emptyList();
            }
            UUID walletId = workMap.keySet().iterator().next();
            return workMap.remove(walletId);
        }
    }


}
