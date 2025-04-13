package ru.vels.test.service.grouped;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GroupedBalanceThread extends Thread {

    private final GroupedBalanceWorkerService groupedBalanceWorkerService;
    private final GroupedBalanceService groupedBalanceService;

    @Override
    public void run() {
        log.info("worker initialized");
        while (true) {
            try {
                runImpl();
            } catch (Exception e) {
                log.error("Exception while processing of group", e);
            }
        }
    }

    private void runImpl() {
        List<BalanceCompletableFuture> futures = groupedBalanceWorkerService.waitAndReceiveWork();
        if (CollectionUtils.isEmpty(futures)) {
            return;
        }
        log.info("worker received {} jobs", futures.size());
        List<Pair<BalanceCompletableFuture, WorkResult>> results = groupedBalanceService.executeWorkGroup(futures);
        log.info("worker completed {} jobs", futures.size());
        for (Pair<BalanceCompletableFuture, WorkResult> result : results) {
            BalanceCompletableFuture future = result.getLeft();
            WorkResult workResult = result.getRight();
            if (workResult.getThrowable() == null) {
                future.complete(workResult);
            } else {
                future.completeExceptionally(workResult.getThrowable());
            }
        }
        log.info("worker send answers for {} jobs", futures.size());
    }

}
