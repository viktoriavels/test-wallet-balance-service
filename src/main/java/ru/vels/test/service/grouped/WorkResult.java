package ru.vels.test.service.grouped;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WorkResult {
    private BigDecimal amount;
    private Throwable throwable;
}
