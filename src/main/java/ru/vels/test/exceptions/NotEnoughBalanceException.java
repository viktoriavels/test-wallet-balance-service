package ru.vels.test.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.CONFLICT,
        reason = "The balance does not have enough money for this operation"
)
public class NotEnoughBalanceException extends RuntimeException {
}
