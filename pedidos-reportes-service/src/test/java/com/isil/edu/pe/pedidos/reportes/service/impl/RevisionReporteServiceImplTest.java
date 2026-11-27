package com.isil.edu.pe.pedidos.reportes.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.pedidos.reportes.repository.RevisionReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ConteoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** PA4: los cálculos del resumen están en el Service (Mockito, sin base de datos). */
class RevisionReporteServiceImplTest {

  private RevisionReporteJdbcRepository repository;
  private RevisionReporteServiceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(RevisionReporteJdbcRepository.class);
    service = new RevisionReporteServiceImpl(repository);
  }

  @Test
  @DisplayName("Return zero percent from RevisionReporteService when there are no orders")
  void returnZeroPercentFromRevisionReporteServiceWhenThereAreNoOrders() {
    // Arrange
    when(repository.contarRevisiones()).thenReturn(new ConteoRevisionProjection(0, 0));

    // Act
    EstadisticasRevision estadisticas = service.obtenerEstadisticas();

    // Assert: no se divide entre cero.
    assertEquals(0, estadisticas.totalPendientes());
    assertEquals(new BigDecimal("0.00"), estadisticas.porcentajeRevisado());
  }

  @Test
  @DisplayName("Round percentage to two decimals in RevisionReporteService when division is not exact")
  void roundPercentageToTwoDecimalsInRevisionReporteServiceWhenDivisionIsNotExact() {
    // Arrange
    when(repository.contarRevisiones()).thenReturn(new ConteoRevisionProjection(3, 2));

    // Act
    EstadisticasRevision estadisticas = service.obtenerEstadisticas();

    // Assert: 2 de 3 = 66.666... -> 66.67
    assertEquals(1, estadisticas.totalPendientes());
    assertEquals(new BigDecimal("66.67"), estadisticas.porcentajeRevisado());
  }
}
