package com.vendingmachine.exception;

public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(int deficit) {
        super(String.format("Please insert %d more cents or cancel transaction.", deficit));
    }
}
