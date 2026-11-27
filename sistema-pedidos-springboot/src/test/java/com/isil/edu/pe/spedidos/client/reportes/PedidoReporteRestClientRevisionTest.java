package com.isil.edu.pe.spedidos.client.reportes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.RevisionPedidoResponse;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** PA4: el cliente HTTP traduce las respuestas y las caídas del servicio de reportes. */
class PedidoReporteRestClientRevisionTest {

  private static final String BASE = "http://reportes.test/api/v1/pedidos";

  private MockRestServiceServer servidor;
  private PedidoReporteRestClient cliente;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    servidor = MockRestServiceServer.bindTo(builder).build();
    cliente = new PedidoReporteRestClient(builder.build(), BASE);
  }

  @Test
  @DisplayName("Return DTOs from PedidoReporteRestClient when reportes service responds 200")
  void returnDtosFromPedidoReporteRestClientWhenReportesServiceResponds200() {
    // Arrange
    servidor.expect(requestTo(BASE + "/estadisticas/revisiones")).andRespond(withSuccess(
        "{\"totalPedidos\":3,\"totalRevisados\":1,\"totalPendientes\":2,\"porcentajeRevisado\":33.33}",
        MediaType.APPLICATION_JSON));
    servidor.expect(requestTo(BASE + "/revisiones?estado=REVISADO")).andRespond(withSuccess(
        "[{\"pedidoId\":7,\"cliente\":\"Ana Torres\",\"productoNombre\":\"Laptop\",\"total\":5000.00,"
            + "\"estado\":\"REVISADO\",\"observacion\":\"OK\",\"fechaRevision\":\"2026-11-26T10:15:00\"}]",
        MediaType.APPLICATION_JSON));

    // Act
    EstadisticasRevisionResponse estadisticas = cliente.obtenerEstadisticasRevision();
    List<RevisionPedidoResponse> revisiones = cliente.listarRevisiones("REVISADO");

    // Assert
    assertEquals(2, estadisticas.totalPendientes());
    assertEquals(0, new BigDecimal("33.33").compareTo(estadisticas.porcentajeRevisado()));
    assertEquals("OK", revisiones.get(0).observacion());
    servidor.verify();
  }

  @Test
  @DisplayName("Translate 503 from reportes service to controlled exception in PedidoReporteRestClient")
  void translate503FromReportesServiceToControlledExceptionInPedidoReporteRestClient() {
    // Arrange
    servidor.expect(requestTo(BASE + "/estadisticas/revisiones"))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    // Act
    PedidoReporteNoDisponibleException error = assertThrows(PedidoReporteNoDisponibleException.class,
        () -> cliente.obtenerEstadisticasRevision());

    // Assert: mensaje para el usuario, sin la traza técnica.
    assertTrue(error.getMessage().contains("8083"));
    assertFalse(error.getMessage().contains("Exception"));
  }

  @Test
  @DisplayName("Translate refused connection to controlled exception in PedidoReporteRestClient")
  void translateRefusedConnectionToControlledExceptionInPedidoReporteRestClient() {
    // Arrange: nadie escucha en el puerto 1, como cuando el servicio está apagado.
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofMillis(500));
    PedidoReporteRestClient clienteSinServicio = new PedidoReporteRestClient(
        RestClient.builder().requestFactory(factory).build(), "http://localhost:1/api/v1/pedidos");

    // Act / Assert
    assertThrows(PedidoReporteNoDisponibleException.class, () -> clienteSinServicio.listarRevisiones(""));
  }

  @Test
  @DisplayName("Translate 400 from reportes service to validation error in PedidoReporteRestClient")
  void translate400FromReportesServiceToValidationErrorInPedidoReporteRestClient() {
    // Arrange
    servidor.expect(requestTo(BASE + "/revisiones?estado=OTRO")).andRespond(withBadRequest());

    // Act / Assert: un 400 no es una caída del servicio; se informa como dato inválido.
    assertThrows(PedidoException.class, () -> cliente.listarRevisiones("OTRO"));
  }
}
