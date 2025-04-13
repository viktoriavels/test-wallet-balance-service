package ru.vels.test.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Valid
public class ChangeBalanceRequest {
    @NotNull
    private UUID walletId;
    @NotNull
    private ChangeBalanceOperation operationType;
    @Positive
    @NotNull
    private BigDecimal amount;
}
