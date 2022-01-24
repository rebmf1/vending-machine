package com.vendingmachine.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum Denomination {

    ONE_CENT(1),
    FIVE_CENTS(5),
    TEN_CENTS(10),
    TWENTY_FIVE_CENTS(25),
    FIFTY_CENTS(50),
    ONE_DOLLAR(100);

    public final int totalCents;

    private Denomination(int totalCents) {
        this.totalCents = totalCents;
    }

    public static List<Denomination> ordered() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(denomination -> denomination.totalCents))
                .collect(Collectors.toList());
    }
}
