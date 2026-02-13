package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StoreResourceTest {

  // --- GET /store ---

  @Test
  @Order(1)
  void shouldListAllStoresSortedByName() {
    // When / Then — import.sql seeds 3 stores
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3));
  }

  // --- GET /store/{id} ---

  @Test
  @Order(2)
  void shouldReturnStoreById() {
    // When / Then — store id=1 is TONSTAD from import.sql
    given()
        .when()
        .get("/store/1")
        .then()
        .statusCode(200)
        .body("name", is("TONSTAD"))
        .body("quantityProductsInStock", is(10));
  }

  @Test
  @Order(3)
  void shouldReturn404WhenStoreNotFound() {
    given()
        .when()
        .get("/store/999")
        .then()
        .statusCode(404);
  }

  // --- POST /store ---

  @Test
  @Order(4)
  void shouldCreateStoreAndReturn201() {
    // Given
    String body = "{\"name\": \"TEST_CREATE_STORE\", \"quantityProductsInStock\": 42}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", is("TEST_CREATE_STORE"))
        .body("quantityProductsInStock", is(42));
  }

  @Test
  @Order(5)
  void shouldReturn422WhenCreatingStoreWithIdSet() {
    // Given
    String body = "{\"id\": 100, \"name\": \"INVALID\", \"quantityProductsInStock\": 1}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/store")
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set on request"));
  }

  // --- PUT /store/{id} ---

  @Test
  @Order(6)
  void shouldUpdateStore() {
    // Given — update store id=2 (KALLAX)
    String body = "{\"name\": \"KALLAX_UPDATED\", \"quantityProductsInStock\": 99}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("/store/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX_UPDATED"))
        .body("quantityProductsInStock", is(99));
  }

  @Test
  @Order(7)
  void shouldReturn422WhenUpdatingWithNullName() {
    // Given
    String body = "{\"quantityProductsInStock\": 5}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("/store/2")
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set on request"));
  }

  @Test
  @Order(8)
  void shouldReturn404WhenUpdatingNonExistentStore() {
    // Given
    String body = "{\"name\": \"DOESNOTMATTER\", \"quantityProductsInStock\": 1}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("/store/999")
        .then()
        .statusCode(404);
  }

  // --- PATCH /store/{id} ---

  @Test
  @Order(9)
  void shouldPatchStore() {
    // Given — patch store id=2 (KALLAX_UPDATED from earlier)
    String body = "{\"name\": \"KALLAX_PATCHED\", \"quantityProductsInStock\": 50}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .patch("/store/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX_PATCHED"));
  }

  @Test
  @Order(10)
  void shouldReturn422WhenPatchingWithNullName() {
    // Given
    String body = "{\"quantityProductsInStock\": 5}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .patch("/store/2")
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set on request"));
  }

  @Test
  @Order(11)
  void shouldReturn404WhenPatchingNonExistentStore() {
    // Given
    String body = "{\"name\": \"DOESNOTMATTER\", \"quantityProductsInStock\": 1}";

    // When / Then
    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .patch("/store/999")
        .then()
        .statusCode(404);
  }

  // --- DELETE /store/{id} ---

  @Test
  @Order(12)
  void shouldDeleteStore() {
    // Given — delete store id=3 (BESTÅ)
    given()
        .when()
        .delete("/store/3")
        .then()
        .statusCode(204);

    // Verify it's gone
    given()
        .when()
        .get("/store/3")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(13)
  void shouldReturn404WhenDeletingNonExistentStore() {
    given()
        .when()
        .delete("/store/999")
        .then()
        .statusCode(404);
  }
}
