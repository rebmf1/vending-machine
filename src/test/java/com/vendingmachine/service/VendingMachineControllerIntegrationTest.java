package com.vendingmachine.service;

import com.google.common.collect.ImmutableSet;
import com.vendingmachine.config.Application;
import com.vendingmachine.domain.ItemType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.GsonMapper;
import io.restassured.mapper.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VendingMachineControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    public void testGetItems() {
        assertThat(RestAssured.given()
                .port(port)
                .get("/items")
                .then()
                .contentType(ContentType.JSON)
                .extract()
                .as(Set.class), Matchers.is(ImmutableSet.of(ItemType.COCA_COLA.name(), ItemType.CRISPS.name(), ItemType.MARS_BAR.name())));
    }

    @Test
    public void testGetItemPrice() {
        assertThat(RestAssured.given()
                .port(port)
                .get("/items/COCA_COLA/price")
                .then()
                .extract()
                .as(Integer.class), Matchers.is(ItemType.COCA_COLA.price));
    }

    @Test
    public void testBuyItem() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"FIFTY_CENTS\": 2}")
                .post("/items/CRISPS")
                .then()
                .contentType(ContentType.JSON)
                .body("FIFTY_CENTS", Matchers.is(1));
    }

    @Test
    public void testBuyItemAndAddCoins() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"TEN_CENTS\": 2}")
                .post("/items/CRISPS")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"TEN_CENTS\": 4}")
                .post("/coins")
                .then()
                .body("TEN_CENTS", Matchers.is(1));
    }

    @Test
    public void testBuyItemAndReturnCoins() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"TEN_CENTS\": 2}")
                .post("/items/CRISPS")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured.given()
                .port(port)
                .delete("/coins")
                .then()
                .body("TEN_CENTS", Matchers.is(2));
    }

    @Test
    public void testLoadChange() {
        int change = RestAssured.given()
                .port(port)
                .get("/change")
                .then()
                .extract()
                .as(Integer.class);

        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"TEN_CENTS\": 2}")
                .post("/change")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertThat(RestAssured.given()
                .port(port)
                .get("/change")
                .then()
                .extract()
                .as(Integer.class), Matchers.is(change + 20));
    }

    @Test
    public void testLoadItems() {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body("{\"COCA_COLA\": 2}")
                .post("/items")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
