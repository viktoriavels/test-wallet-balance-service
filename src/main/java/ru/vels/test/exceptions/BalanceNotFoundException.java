package ru.vels.test.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.NOT_FOUND,
        reason = "The balance with entered id is not exists"
)
public class BalanceNotFoundException extends RuntimeException {
}
