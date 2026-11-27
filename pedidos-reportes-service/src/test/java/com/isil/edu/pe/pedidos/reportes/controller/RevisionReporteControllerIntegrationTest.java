package com.isil.edu.pe.pedidos.reportes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** PA4: contrato REST de los dos recursos nuevos (Controller + Service + JDBC contra H2). */
@SpringBootTest
@ActiveProfiles("test")
@Sql("/revisiones-datos.sql")
class RevisionReporteControllerIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @DisplayName("Return reviewed orders with observation from revisiones endpoint when estado is REVISADO")
  void returnReviewedOrdersWithObservationFromRevisionesEndpointWhenEstadoIsRevisado() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "REVISADO"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].pedidoId").value(1))
        .andExpect(jsonPath("$[0].estado").value("REVISADO"))
        .andExpect(jsonPath("$[0].observacion").value("Datos verificados con el cliente"))
        .andExpect(jsonPath("$[0].fechaRevision").value("2026-11-26T10:15:00"));
  }

  @Test
  @DisplayName("Return pending orders without observation from revisiones endpoint when estado is pendiente")
  void returnPendingOrdersWithoutObservationFromRevisionesEndpointWhenEstadoIsPendiente() throws Exception {
    // Arrange / Act: también acepta minúsculas.
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "pendiente"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
        .andExpect(jsonPath("$[0].observacion").doesNotExist());
  }

  @Test
  @DisplayName("Return statistics from estadisticas endpoint when orders exist")
  void returnStatisticsFromEstadisticasEndpointWhenOrdersExist() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/estadisticas/revisiones"));

    // Assert: 1 de 3 revisados = 33.33 %.
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPedidos").value(3))
        .andExpect(jsonPath("$.totalRevisados").value(1))
        .andExpect(jsonPath("$.totalPendientes").value(2))
        .andExpect(jsonPath("$.porcentajeRevisado").value(33.33));
  }

  @Test
  @DisplayName("Return 400 from revisiones endpoint when estado is invalid")
  void return400FromRevisionesEndpointWhenEstadoIsInvalid() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "ANULADO"));

    // Assert
    response.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Parámetro inválido"))
        .andExpect(jsonPath("$.detail").value("El estado 'ANULADO' no es válido. Use PENDIENTE o REVISADO."));
  }
}
