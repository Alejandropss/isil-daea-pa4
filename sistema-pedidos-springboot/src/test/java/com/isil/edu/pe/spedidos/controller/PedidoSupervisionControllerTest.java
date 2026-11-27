package com.isil.edu.pe.spedidos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** PA4: Controller MVC + Service + JDBC (H2) con los filtros reales de Spring Security. */
@SpringBootTest
@ActiveProfiles("test")
class PedidoSupervisionControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockitoBean
  private PedidoReporteClient reporteClient;

  private long pedidoId;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    limpiar();
    pedidoId = pedidoRepository.saveAndFlush(new Pedido("Carlos Rojas", "MON-001", "Monitor",
        new BigDecimal("850.00"), 1, new BigDecimal("850.00"))).getId();
  }

  @AfterEach
  void tearDown() {
    limpiar();
  }

  @Test
  @DisplayName("Redirect to supervision in PedidoSupervisionController after OPERADOR reviews with CSRF")
  void redirectToSupervisionInPedidoSupervisionControllerAfterOperadorReviewsWithCsrf() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
        .param("observacion", "Revisado por supervisión")
        .with(user("operador").roles("OPERADOR"))
        .with(csrf()));

    // Assert: Post/Redirect/Get y la revisión quedó guardada.
    response.andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/supervision/pedidos"))
        .andExpect(flash().attributeExists("mensaje"));
    assertEquals(1, contarRevisiones());
  }

  @Test
  @DisplayName("Reject review in PedidoSupervisionController when CONSULTA sends valid CSRF")
  void rejectReviewInPedidoSupervisionControllerWhenConsultaSendsValidCsrf() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
        .param("observacion", "Intento sin permiso")
        .with(user("consulta").roles("CONSULTA"))
        .with(csrf()));

    // Assert: lo corta Spring Security por rol; no llega al Service.
    response.andExpect(status().isForbidden());
    assertEquals(0, contarRevisiones());
  }

  @Test
  @DisplayName("Reject review in PedidoSupervisionController when OPERADOR omits CSRF token")
  void rejectReviewInPedidoSupervisionControllerWhenOperadorOmitsCsrfToken() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
        .param("observacion", "Sin token")
        .with(user("operador").roles("OPERADOR")));

    // Assert
    response.andExpect(status().isForbidden());
    assertEquals(0, contarRevisiones());
  }

  @Test
  @DisplayName("Return 409 from PedidoSupervisionController when order is already reviewed")
  void return409FromPedidoSupervisionControllerWhenOrderIsAlreadyReviewed() throws Exception {
    // Arrange
    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId).param("observacion", "Primera")
        .with(user("operador").roles("OPERADOR")).with(csrf()));

    // Act
    ResultActions response = mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
        .param("observacion", "Segunda")
        .with(user("operador").roles("OPERADOR"))
        .with(csrf()));

    // Assert
    response.andExpect(status().isConflict())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attributeExists("error"));
    assertEquals(1, contarRevisiones());
  }

  @Test
  @DisplayName("Show local orders in PedidoSupervisionController when reportes service is down")
  void showLocalOrdersInPedidoSupervisionControllerWhenReportesServiceIsDown() throws Exception {
    // Arrange
    when(reporteClient.obtenerEstadisticasRevision()).thenThrow(new PedidoReporteNoDisponibleException(
        "pedidos-reportes-service no está disponible. Verifique que se ejecute en el puerto 8083.", null));

    // Act
    ResultActions response = mockMvc.perform(get("/supervision/pedidos")
        .with(user("consulta").roles("CONSULTA")));

    // Assert: la pantalla responde, muestra el aviso y la lista local sigue disponible.
    response.andExpect(status().isOk())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attributeExists("errorReportes", "pedidos"));
  }

  private int contarRevisiones() {
    Integer total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?", Integer.class, pedidoId);
    return total == null ? 0 : total;
  }

  private void limpiar() {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");
  }
}
