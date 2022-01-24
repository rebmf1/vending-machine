package com.vendingmachine.service;

import com.google.common.collect.ImmutableMap;
import com.vendingmachine.domain.Denomination;
import com.vendingmachine.domain.ItemType;
import com.vendingmachine.exception.InsufficientChangeException;
import com.vendingmachine.exception.InsufficientFundsException;
import com.vendingmachine.exception.ItemNotAvailableException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class VendingMachine {

    private final Map<ItemType, Integer> itemInventory;
    private final Map<Denomination, Integer> change;
    private final Map<Denomination, Integer> coinsForCurrentTransaction = new HashMap<>();
    private Optional<ItemType> pendingItem = Optional.empty();

    public VendingMachine(Map<ItemType, Integer> initialInventory, Map<Denomination, Integer> initialChange) {
        this.itemInventory = initialInventory;
        this.change = initialChange;
    }

    public Set<ItemType> availableItems() {
        return itemInventory.entrySet().stream()
                .filter(item -> item.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public int getPrice(ItemType itemType) {
        return itemType.price;
    }

    public Map<Denomination, Integer> buyItem(ItemType itemType, Map<Denomination, Integer> coins) throws InsufficientFundsException, InsufficientChangeException, ItemNotAvailableException {
        loadCoinsForCurrentTransaction(coins);
        pendingItem = Optional.of(itemType);
        return buyItem();
    }

    private Map<Denomination, Integer> buyItem() throws InsufficientFundsException, InsufficientChangeException, ItemNotAvailableException {
        ItemType itemType = pendingItem.orElseThrow();
        if (itemInventory.getOrDefault(itemType, 0) == 0) {
            throw new ItemNotAvailableException();
        }
        int totalPaid = calculateTotal(coinsForCurrentTransaction);
        if (totalPaid < itemType.price) {
            throw new InsufficientFundsException(itemType.price - totalPaid);
        } else {
            int changeOwed = totalPaid - itemType.price;
            int availableChange = calculateTotal(change);
            if (availableChange < changeOwed) {
                throw new InsufficientChangeException();
            }
            Map<Denomination, Integer> changeToPay = calculateChange(changeOwed);
            releaseItem(itemType);
            return changeToPay;
        }
    }

    public Map<Denomination, Integer> addCoins(Map<Denomination, Integer> additionalCoins) throws InsufficientFundsException, ItemNotAvailableException, InsufficientChangeException {
        loadCoinsForCurrentTransaction(additionalCoins);
        return buyItem();
    }

    private void loadCoinsForCurrentTransaction(Map<Denomination, Integer> coins) {
        coins.forEach((coin, count) -> {
            coinsForCurrentTransaction.putIfAbsent(coin, 0);
            coinsForCurrentTransaction.put(coin, coinsForCurrentTransaction.get(coin) + count);
        });
        loadChange(coins);
    }

    public Map<Denomination, Integer> returnCoins() {
        // assume this also returns coins to customer
        Map<Denomination, Integer> returnedCoins = ImmutableMap.copyOf(coinsForCurrentTransaction);
        coinsForCurrentTransaction.forEach((denomination, count) -> {
            change.put(denomination, change.get(denomination) - count);
        });
        coinsForCurrentTransaction.clear();
        pendingItem = Optional.empty();
        return returnedCoins;
    }

    public void loadChange(Map<Denomination, Integer> extraChange) {
        extraChange.forEach((denomination, count) -> {
            change.putIfAbsent(denomination, 0);
            change.put(denomination, change.get(denomination) + count);
        });
    }

    public void loadItems(Map<ItemType, Integer> extraItems) {
        extraItems.forEach((itemType, count) -> {
            itemInventory.putIfAbsent(itemType, 0);
            itemInventory.put(itemType, itemInventory.get(itemType) + count);
        });
    }

    private void releaseItem(ItemType itemType) {
        // assume this also releases item to customer
        itemInventory.put(itemType, itemInventory.get(itemType) - 1);
    }

    private int calculateTotal(Map<Denomination, Integer> coins) {
        return coins.entrySet().stream()
                .map(coin -> coin.getKey().totalCents * coin.getValue())
                .reduce(0, Integer::sum);
    }

    private Map<Denomination, Integer> calculateChange(int changeOwed) throws InsufficientChangeException {
        return calculateChange(new HashMap<>(), changeOwed);
    }

    private Map<Denomination, Integer> calculateChange(Map<Denomination, Integer> changeBreakdown, int changeOwed) throws InsufficientChangeException {
        if (changeOwed == calculateTotal(changeBreakdown)) {
            return changeBreakdown;
        }
        Denomination coinAvailable = Denomination.ordered().stream()
                .filter(denomination -> denomination.totalCents <= changeOwed)
                .filter(denomination -> change.getOrDefault(denomination, 0) > 0)
                .findFirst()
                .orElseThrow(InsufficientChangeException::new);
        change.put(coinAvailable, change.get(coinAvailable) - 1);
        changeBreakdown.putIfAbsent(coinAvailable, 0);
        changeBreakdown.put(coinAvailable, change.get(coinAvailable) + 1);
        return calculateChange(changeBreakdown, changeOwed);
    }
}
