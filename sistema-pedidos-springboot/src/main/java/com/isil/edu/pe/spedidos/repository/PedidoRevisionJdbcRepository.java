package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.service.domain.PedidoSupervision;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * PA4: SQL de la revisión de pedidos. Todos los valores viajan como parámetros con nombre.
 */
@Repository
public class PedidoRevisionJdbcRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public PedidoRevisionJdbcRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Vacío si el pedido no existe; si existe, indica si ya fue revisado. */
  public Optional<Boolean> buscarEstadoRevision(long pedidoId) {
    String sql = "SELECT revisado FROM pedido WHERE id = :pedidoId";
    return jdbc.queryForList(sql, Map.of("pedidoId", pedidoId), Boolean.class)
        .stream()
        .findFirst();
  }

  /** Escritura 1: solo cambia pedidos pendientes. Devuelve las filas modificadas (1 o 0). */
  public int marcarRevisado(long pedidoId) {
    String sql = "UPDATE pedido SET revisado = TRUE WHERE id = :pedidoId AND revisado = FALSE";
    return jdbc.update(sql, Map.of("pedidoId", pedidoId));
  }

  /** Escritura 2: deja la evidencia de la revisión. */
  public void insertarRevision(long pedidoId, String observacion, LocalDateTime fechaRevision) {
    String sql = """
        INSERT INTO pedido_revision (pedido_id, observacion, fecha_revision)
        VALUES (:pedidoId, :observacion, :fechaRevision)
        """;
    jdbc.update(sql, Map.of(
        "pedidoId", pedidoId,
        "observacion", observacion,
        "fechaRevision", Timestamp.valueOf(fechaRevision)));
  }

  /** Lista local para la pantalla de supervisión (solo lectura). */
  public List<PedidoSupervision> listarParaSupervision() {
    String sql = """
        SELECT id, cliente, producto_nombre, total, revisado
        FROM pedido
        ORDER BY id DESC
        """;
    return jdbc.query(sql, (rs, rowNum) -> new PedidoSupervision(
        rs.getLong("id"),
        rs.getString("cliente"),
        rs.getString("producto_nombre"),
        rs.getBigDecimal("total"),
        rs.getBoolean("revisado")));
  }
}
