package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    private final BigDecimal available;
    private final BigDecimal required;

    public InsufficientFundsException(BigDecimal available, BigDecimal required) {
        super(String.format("Insufficient funds. Available: %s, Required: %s", available, required));
        this.available = available;
        this.required = required;
    }
}
