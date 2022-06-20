package com.henrique.virtualteacher.exceptions;

public class InsufficientFundsException extends RuntimeException {

    private static final String INSUFFICIENT_FUNDS_EXCEPTION_MESSAGE = "Insufficient funds in wallet";

    public InsufficientFundsException(String message) {
        super(INSUFFICIENT_FUNDS_EXCEPTION_MESSAGE);
    }
}