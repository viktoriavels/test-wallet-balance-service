package ru.vels.test.service.grouped;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("balance-grouped")
@RequiredArgsConstructor
public class GroupedBalanceThreadInitializerBean {
    private final GroupedBalanceServiceProperties properties;
    private final GroupedBalanceWorkerService groupedBalanceWorkerService;
    private final GroupedBalanceService groupedBalanceService;

    @PostConstruct
    public void initWorkerThreads() {
        for (int i = 0; i < properties.getThreadsCount(); i++) {
            new GroupedBalanceThread(groupedBalanceWorkerService, groupedBalanceService).start();
        }
    }
}
