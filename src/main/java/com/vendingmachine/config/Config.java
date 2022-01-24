package com.vendingmachine.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.vendingmachine.domain.Denomination;
import com.vendingmachine.domain.ItemType;
import com.vendingmachine.rest.VendingMachineController;
import com.vendingmachine.service.VendingMachine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class Config {

    private static final Map<ItemType, Integer> INITIAL_INVENTORY = Arrays.stream(ItemType.values())
            .collect(Collectors.toMap(Function.identity(), item -> 10));
    private static final Map<Denomination, Integer> INITIAL_CHANGE = Arrays.stream(Denomination.values())
            .collect(Collectors.toMap(Function.identity(), coin -> 10));

    @Bean
    public VendingMachine vendingMachine(){
        return new VendingMachine(INITIAL_INVENTORY, INITIAL_CHANGE);
    }

    @Bean
    public VendingMachineController vendingMachineController(VendingMachine vendingMachine) {
        return new VendingMachineController(vendingMachine);
    }
}
