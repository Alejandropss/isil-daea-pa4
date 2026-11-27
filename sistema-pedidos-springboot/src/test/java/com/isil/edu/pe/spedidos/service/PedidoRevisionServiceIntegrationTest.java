package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.exception.RevisionDuplicadaException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * PA4: commit y rollback reales contra H2. La clase NO es @Transactional a propósito:
 * así cada revisión hace su propio commit (o rollback) y luego se lee lo que quedó en la base.
 */
@SpringBootTest
@ActiveProfiles("test")
class PedidoRevisionServiceIntegrationTest {

  private static final String FALLA_FORZADA = "FORZAR FALLO EN INSERT";

  @Autowired
  private PedidoRevisionService revisionService;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // Repository real envuelto en un espía para comprobar que el UPDATE sí se ejecutó.
  @MockitoSpyBean
  private PedidoRevisionJdbcRepository revisionRepository;

  private long pedidoId;

  @BeforeEach
  void setUp() {
    limpiar();
    Pedido pedido = new Pedido("Ana Torres", "LAP-001", "Laptop", new BigDecimal("2500.00"), 2,
        new BigDecimal("5000.00"));
    pedidoId = pedidoRepository.saveAndFlush(pedido).getId();
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.execute("ALTER TABLE pedido_revision DROP CONSTRAINT IF EXISTS ck_forzar_fallo");
    limpiar();
  }

  @Test
  @DisplayName("Commit review in PedidoRevisionService when order is pending")
  void commitReviewInPedidoRevisionServiceWhenOrderIsPending() {
    // Arrange
    String observacion = "  Datos verificados con el cliente  ";

    // Act
    revisionService.revisar(pedidoId, observacion);

    // Assert: las dos escrituras quedaron confirmadas.
    assertTrue(estaRevisado());
    assertEquals(1, contarRevisiones());
    assertEquals("Datos verificados con el cliente", jdbcTemplate.queryForObject(
        "SELECT observacion FROM pedido_revision WHERE pedido_id = ?", String.class, pedidoId));
  }

  @Test
  @DisplayName("Rollback order update in PedidoRevisionService when revision insert fails in the database")
  void rollbackOrderUpdateInPedidoRevisionServiceWhenRevisionInsertFailsInTheDatabase() {
    // Arrange: una restricción CHECK hace que la base rechace el INSERT con esta observación.
    jdbcTemplate.execute("ALTER TABLE pedido_revision ADD CONSTRAINT ck_forzar_fallo "
        + "CHECK (observacion <> '" + FALLA_FORZADA + "')");

    // Act
    assertThrows(DataAccessException.class, () -> revisionService.revisar(pedidoId, FALLA_FORZADA));

    // Assert: el UPDATE se ejecutó, pero el rollback lo deshizo y no quedó ninguna revisión.
    verify(revisionRepository, times(1)).marcarRevisado(pedidoId);
    assertFalse(estaRevisado());
    assertEquals(0, contarRevisiones());
  }

  @Test
  @DisplayName("Reject second review in PedidoRevisionService when order is already reviewed")
  void rejectSecondReviewInPedidoRevisionServiceWhenOrderIsAlreadyReviewed() {
    // Arrange
    revisionService.revisar(pedidoId, "Primera revisión");

    // Act
    assertThrows(RevisionDuplicadaException.class, () -> revisionService.revisar(pedidoId, "Segunda revisión"));

    // Assert
    assertTrue(estaRevisado());
    assertEquals(1, contarRevisiones());
  }

  @Test
  @DisplayName("Reject review in PedidoRevisionService when order does not exist")
  void rejectReviewInPedidoRevisionServiceWhenOrderDoesNotExist() {
    // Arrange
    long idInexistente = 999_999L;

    // Act
    assertThrows(PedidoNoEncontradoException.class, () -> revisionService.revisar(idInexistente, "Revisión"));

    // Assert: no se escribió nada.
    assertEquals(0, contarRevisiones());
    assertFalse(estaRevisado());
  }

  private boolean estaRevisado() {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?", Boolean.class, pedidoId));
  }

  private int contarRevisiones() {
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pedido_revision", Integer.class);
    return total == null ? 0 : total;
  }

  private void limpiar() {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");
  }
}
