package com.isil.edu.pe.spedidos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** PA4: las validaciones cortan antes de llegar a la base (Mockito, sin Spring). */
class PedidoRevisionServiceValidacionTest {

  private PedidoRevisionJdbcRepository repository;
  private PedidoRevisionServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(PedidoRevisionJdbcRepository.class);
    service = new PedidoRevisionServiceImpl(repository);
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -5})
  @DisplayName("Reject review in PedidoRevisionService when order id is not positive")
  void rejectReviewInPedidoRevisionServiceWhenOrderIdIsNotPositive(long pedidoId) {
    // Act
    PedidoException error = assertThrows(PedidoException.class, () -> service.revisar(pedidoId, "Revisado"));

    // Assert
    assertEquals("El id del pedido debe ser mayor que cero.", error.getMessage());
    verifyNoInteractions(repository);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "    "})
  @DisplayName("Reject review in PedidoRevisionService when observation is empty")
  void rejectReviewInPedidoRevisionServiceWhenObservationIsEmpty(String observacion) {
    // Act
    PedidoException error = assertThrows(PedidoException.class, () -> service.revisar(1L, observacion));

    // Assert
    assertEquals("La observación es obligatoria.", error.getMessage());
    verifyNoInteractions(repository);
  }

  @Test
  @DisplayName("Reject review in PedidoRevisionService when observation exceeds 200 characters")
  void rejectReviewInPedidoRevisionServiceWhenObservationExceeds200Characters() {
    // Arrange
    String observacion = "x".repeat(201);

    // Act
    PedidoException error = assertThrows(PedidoException.class, () -> service.revisar(1L, observacion));

    // Assert
    assertEquals("La observación no puede superar 200 caracteres.", error.getMessage());
    verifyNoInteractions(repository);
  }
}
