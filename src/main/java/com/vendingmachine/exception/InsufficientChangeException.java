package com.vendingmachine.exception;

public class InsufficientChangeException extends Exception {

    public InsufficientChangeException() {
        super("Not enough change available. Returning coins.");
    }
}
