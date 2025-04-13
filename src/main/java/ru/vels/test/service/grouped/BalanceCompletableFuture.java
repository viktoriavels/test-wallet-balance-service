package ru.vels.test.service.grouped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class BalanceCompletableFuture extends CompletableFuture<WorkResult> {
    private final WorkItem workItem;
}
