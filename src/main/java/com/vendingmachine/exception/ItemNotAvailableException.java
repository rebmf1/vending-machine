package com.vendingmachine.exception;

public class ItemNotAvailableException extends Exception {

    public ItemNotAvailableException() {
        super("Item not available. Returning coins.");
    }
}
