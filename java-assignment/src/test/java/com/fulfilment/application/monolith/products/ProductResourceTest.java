package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProductResourceTest {

  private ProductRepository productRepository;
  private ProductResource resource;

  @BeforeEach
  void setUp() {
    productRepository = Mockito.mock(ProductRepository.class);
    resource = new ProductResource(productRepository);
  }

  // --- GET /product ---

  @Test
  void shouldListAllProductsSortedByName() {
    // Given
    Product p1 = buildProduct(1L, "Alpha", "Desc A", BigDecimal.valueOf(10.00), 5);
    Product p2 = buildProduct(2L, "Beta", "Desc B", BigDecimal.valueOf(20.00), 10);
    when(productRepository.listAll(any(Sort.class))).thenReturn(List.of(p1, p2));

    // When
    List<Product> result = resource.get();

    // Then
    assertEquals(2, result.size());
    assertEquals("Alpha", result.get(0).name);
    assertEquals("Beta", result.get(1).name);
  }

  @Test
  void shouldReturnEmptyListWhenNoProducts() {
    // Given
    when(productRepository.listAll(any(Sort.class))).thenReturn(Collections.emptyList());

    // When
    List<Product> result = resource.get();

    // Then
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  // --- GET /product/{id} ---

  @Test
  void shouldReturnProductWhenFound() {
    // Given
    Product product = buildProduct(1L, "Widget", "A widget", BigDecimal.valueOf(9.99), 100);
    when(productRepository.findById(1L)).thenReturn(product);

    // When
    Product result = resource.getSingle(1L);

    // Then
    assertNotNull(result);
    assertEquals("Widget", result.name);
    assertEquals("A widget", result.description);
    assertEquals(BigDecimal.valueOf(9.99), result.price);
    assertEquals(100, result.stock);
  }

  @Test
  void shouldThrow404WhenProductNotFound() {
    // Given
    when(productRepository.findById(99L)).thenReturn(null);

    // When / Then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getSingle(99L));
    assertEquals(404, exception.getResponse().getStatus());
  }

  // --- POST /product ---

  @Test
  void shouldCreateProduct() {
    // Given
    Product product = new Product("New Product");
    product.description = "New description";
    product.price = BigDecimal.valueOf(15.50);
    product.stock = 50;

    // When
    Response response = resource.create(product);

    // Then
    assertEquals(201, response.getStatus());
    verify(productRepository).persist(product);
  }

  @Test
  void shouldRejectCreateWhenIdIsSet() {
    // Given
    Product product = new Product("Invalid");
    product.id = 5L;

    // When / Then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.create(product));
    assertEquals(422, exception.getResponse().getStatus());
    verify(productRepository, never()).persist(any(Product.class));
  }

  // --- PUT /product/{id} ---

  @Test
  void shouldUpdateProduct() {
    // Given
    Product existing = buildProduct(1L, "Old Name", "Old desc", BigDecimal.valueOf(5.00), 10);
    when(productRepository.findById(1L)).thenReturn(existing);

    Product update = new Product("New Name");
    update.description = "New desc";
    update.price = BigDecimal.valueOf(12.00);
    update.stock = 20;

    // When
    Product result = resource.update(1L, update);

    // Then
    assertEquals("New Name", result.name);
    assertEquals("New desc", result.description);
    assertEquals(BigDecimal.valueOf(12.00), result.price);
    assertEquals(20, result.stock);
    verify(productRepository).persist(existing);
  }

  @Test
  void shouldRejectUpdateWhenNameIsNull() {
    // Given
    Product update = new Product();

    // When / Then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.update(1L, update));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void shouldThrow404WhenUpdatingNonExistentProduct() {
    // Given
    Product update = new Product("Name");
    when(productRepository.findById(99L)).thenReturn(null);

    // When / Then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.update(99L, update));
    assertEquals(404, exception.getResponse().getStatus());
  }

  // --- DELETE /product/{id} ---

  @Test
  void shouldDeleteProduct() {
    // Given
    Product existing = buildProduct(1L, "To Delete", null, null, 0);
    when(productRepository.findById(1L)).thenReturn(existing);

    // When
    Response response = resource.delete(1L);

    // Then
    assertEquals(204, response.getStatus());
    verify(productRepository).delete(existing);
  }

  @Test
  void shouldThrow404WhenDeletingNonExistentProduct() {
    // Given
    when(productRepository.findById(99L)).thenReturn(null);

    // When / Then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.delete(99L));
    assertEquals(404, exception.getResponse().getStatus());
  }

  // --- helpers ---

  private Product buildProduct(Long id, String name, String description, BigDecimal price, int stock) {
    Product p = new Product(name);
    p.id = id;
    p.description = description;
    p.price = price;
    p.stock = stock;
    return p;
  }
}
