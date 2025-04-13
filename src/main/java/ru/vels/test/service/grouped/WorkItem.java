package ru.vels.test.service.grouped;

import lombok.Builder;
import lombok.Data;
import ru.vels.test.dto.ChangeBalanceOperation;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WorkItem {
    private WorkType workType;
    private UUID walletId;
    private ChangeBalanceOperation operation;
    private BigDecimal amount;
}
