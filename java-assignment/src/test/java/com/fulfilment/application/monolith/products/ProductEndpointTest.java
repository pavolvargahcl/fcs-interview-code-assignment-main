package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {


  @Test
  void testCrudProduct() {
    // Given
    final String path = "product";

    given()
      .when()
      .get(path)
      .then()
      .statusCode(200)
      .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // When
    given().when().delete(path + "/1").then().statusCode(204);

    // Then
    given()
      .when()
      .get(path)
      .then()
      .statusCode(200)
      .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }
}
