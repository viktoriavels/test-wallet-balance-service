package ru.vels.test.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.vels.test.dto.ChangeBalanceRequest;
import ru.vels.test.dto.GetBalanceResponse;
import ru.vels.test.service.BalanceService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping(
            value = "/wallet",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void changeBalance(@Validated @RequestBody ChangeBalanceRequest request) {
        balanceService.changeBalance(request.getWalletId(), request.getOperationType(), request.getAmount());
    }

    @GetMapping(
            value = "/wallets/{walletId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public GetBalanceResponse fetchBalance(@PathVariable("walletId") UUID walletId) {
        return new GetBalanceResponse(balanceService.fetchBalance(walletId));
    }

}
