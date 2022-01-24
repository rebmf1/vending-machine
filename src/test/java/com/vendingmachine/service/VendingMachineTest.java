package com.vendingmachine.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.vendingmachine.domain.Denomination;
import com.vendingmachine.domain.ItemType;
import com.vendingmachine.exception.InsufficientChangeException;
import com.vendingmachine.exception.InsufficientFundsException;
import com.vendingmachine.exception.ItemNotAvailableException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class VendingMachineTest {

    private VendingMachine vendingMachine;

    @Test
    public void testCheckAvailableItems() {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                ItemType.COCA_COLA, 2,
                ItemType.CRISPS, 1
        )), Maps.newHashMap());
        assertThat(vendingMachine.availableItems(), is(ImmutableSet.of(ItemType.COCA_COLA, ItemType.CRISPS)));
    }

    @Test
    public void testGetItemPrice() {
        vendingMachine = new VendingMachine(Maps.newHashMap(), Maps.newHashMap());
        assertThat(vendingMachine.getPrice(ItemType.MARS_BAR), is(ItemType.MARS_BAR.price));
    }

    @Test
    public void testBuyUnavailableItem() {
        vendingMachine = new VendingMachine(Maps.newHashMap(), Maps.newHashMap());
        assertThrows(ItemNotAvailableException.class, () -> vendingMachine.buyItem(ItemType.COCA_COLA, Maps.newHashMap()));
    }

    @Test
    public void testBuyItemWithInsufficientFunds() {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        assertThrows(InsufficientFundsException.class, () -> vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.TEN_CENTS, 1)));
    }

    @Test
    public void testBuyItemWithInsufficientFundsAddMoreCoins() throws InsufficientChangeException, ItemNotAvailableException, InsufficientFundsException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        try {
            vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.TEN_CENTS, 1));
            fail();
        } catch (InsufficientFundsException e) {
            assertThat(vendingMachine.addCoins(ImmutableMap.of(Denomination.TEN_CENTS, 4)), is(ImmutableMap.of()));
        }
    }

    @Test
    public void testBuyItemWithInsufficientFundsReturnItem() throws InsufficientChangeException, ItemNotAvailableException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        try {
            vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.TEN_CENTS, 1));
            fail();
        } catch (InsufficientFundsException e) {
            assertThat(vendingMachine.returnCoins(), is(ImmutableMap.of(Denomination.TEN_CENTS, 1)));
        }
    }

    @Test
    public void testBuyItemWithInsufficientChange() {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        assertThrows(InsufficientChangeException.class, () -> vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.ONE_DOLLAR, 1)));
    }

    @Test
    public void testBuyItemWithPreviouslyInsufficientChange() throws InsufficientChangeException, ItemNotAvailableException, InsufficientFundsException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        assertThat(vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.TEN_CENTS, 6)), is(ImmutableMap.of(Denomination.TEN_CENTS, 1)));
    }

    @Test
    public void testBuyItemWithCorrectChange() throws InsufficientFundsException, ItemNotAvailableException, InsufficientChangeException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        assertThat(vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.TEN_CENTS, 5)), is(ImmutableMap.of()));
        assertThat(vendingMachine.availableItems(), is(ImmutableSet.of(ItemType.COCA_COLA)));
    }

    @Test
    public void testBuyItemWithInsufficientChangeThenLoadChange() throws ItemNotAvailableException, InsufficientFundsException, InsufficientChangeException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        try {
            vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.ONE_DOLLAR, 1));
        } catch (InsufficientChangeException e) {
            vendingMachine.returnCoins();
            vendingMachine.loadChange(ImmutableMap.of(Denomination.FIFTY_CENTS, 1));
            assertThat(vendingMachine.buyItem(ItemType.CRISPS, ImmutableMap.of(Denomination.ONE_DOLLAR, 1)), is(ImmutableMap.of(Denomination.FIFTY_CENTS, 1)));
        }
    }

    @Test
    public void testLoadItems() {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        vendingMachine.loadItems(ImmutableMap.of(ItemType.MARS_BAR, 1));
        assertThat(vendingMachine.availableItems(), is(ImmutableSet.of(ItemType.COCA_COLA, ItemType.CRISPS, ItemType.MARS_BAR)));
    }

    @Test
    public void testBuyAllOfAnItem() throws InsufficientFundsException, ItemNotAvailableException, InsufficientChangeException {
        vendingMachine = new VendingMachine(Maps.newHashMap(
                ImmutableMap.of(
                        ItemType.COCA_COLA, 2,
                        ItemType.CRISPS, 1
                )), Maps.newHashMap());
        vendingMachine.buyItem(ItemType.COCA_COLA, ImmutableMap.of(Denomination.FIFTY_CENTS, 1, Denomination.ONE_DOLLAR, 1));
        assertThat(vendingMachine.availableItems(), is(ImmutableSet.of(ItemType.CRISPS, ItemType.COCA_COLA)));
        vendingMachine.buyItem(ItemType.COCA_COLA, ImmutableMap.of(Denomination.FIFTY_CENTS, 1, Denomination.ONE_DOLLAR, 1));
        assertThat(vendingMachine.availableItems(), is(ImmutableSet.of(ItemType.CRISPS)));
    }
}
