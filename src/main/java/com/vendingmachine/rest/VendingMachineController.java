package com.vendingmachine.rest;

import com.vendingmachine.domain.Denomination;
import com.vendingmachine.domain.ItemType;
import com.vendingmachine.exception.InsufficientChangeException;
import com.vendingmachine.exception.InsufficientFundsException;
import com.vendingmachine.exception.ItemNotAvailableException;
import com.vendingmachine.service.VendingMachine;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(path = "/")
public class VendingMachineController {

    private final VendingMachine vendingMachine;

    public VendingMachineController(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @ExceptionHandler({InsufficientFundsException.class, InsufficientChangeException.class, ItemNotAvailableException.class})
    public ResponseEntity<Object> handleException(Exception exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/items", produces = "application/json")
    public Set<ItemType> getAvailableItems() {
        return vendingMachine.availableItems();
    }

    @GetMapping("/items/{itemType}/price")
    public int getItemPrice(@PathVariable ItemType itemType) {
        return vendingMachine.getPrice(itemType);
    }

    @PostMapping(value = "/items/{itemType}", consumes = "application/json", produces = "application/json")
    public Map<Denomination, Integer> buyItem(@PathVariable ItemType itemType, @RequestBody Map<Denomination, Integer> coins) throws InsufficientFundsException, ItemNotAvailableException, InsufficientChangeException {
        return vendingMachine.buyItem(itemType, coins);
    }

    @PostMapping(value = "/coins", consumes = "application/json", produces = "application/json")
    public Map<Denomination, Integer> addCoins(@RequestBody Map<Denomination, Integer> coins) throws InsufficientFundsException, InsufficientChangeException, ItemNotAvailableException {
        return vendingMachine.addCoins(coins);
    }

    @DeleteMapping(value = "/coins", produces = "application/json")
    public Map<Denomination, Integer> returnCoins() {
        return vendingMachine.returnCoins();
    }

    @PostMapping(value = "/change", consumes = "application/json")
    public void loadChange(@RequestBody Map<Denomination, Integer> change) {
        vendingMachine.loadChange(change);
    }

    @GetMapping(value = "/change")
    public int getChange() {
        return vendingMachine.getChange();
    }

    @PostMapping(value = "/items", consumes = "application/json")
    public void loadItems(@RequestBody Map<ItemType, Integer> items) {
        vendingMachine.loadItems(items);
    }
}
