package com.vendingmachine.domain;

public enum ItemType {

    CRISPS(50),
    COCA_COLA(150),
    MARS_BAR(75);

    public final int price;

    private ItemType(int price) {
        this.price = price;
    }
}
