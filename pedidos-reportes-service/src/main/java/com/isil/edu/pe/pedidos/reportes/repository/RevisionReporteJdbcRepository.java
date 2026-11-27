package com.isil.edu.pe.pedidos.reportes.repository;

import com.isil.edu.pe.pedidos.reportes.repository.projection.ConteoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadoRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.RevisionPedido;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * PA4: consultas de revisión. El filtro de estado viaja como parámetro (:revisado);
 * solo se elige entre dos textos SQL fijos, nunca se concatena lo que envía el usuario.
 */
@Repository
public class RevisionReporteJdbcRepository {

  private static final String SELECT_REVISIONES = """
      SELECT p.id, p.cliente, p.producto_nombre, p.total, p.revisado, r.observacion, r.fecha_revision
      FROM pedido p
      LEFT JOIN pedido_revision r ON r.pedido_id = p.id
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public RevisionReporteJdbcRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<RevisionPedido> listarRevisiones() {
    return jdbc.query(SELECT_REVISIONES + " ORDER BY p.id DESC", this::mapear);
  }

  public List<RevisionPedido> listarRevisionesPorEstado(EstadoRevision estado) {
    String sql = SELECT_REVISIONES + " WHERE p.revisado = :revisado ORDER BY p.id DESC";
    return jdbc.query(sql, Map.of("revisado", estado == EstadoRevision.REVISADO), this::mapear);
  }

  public ConteoRevisionProjection contarRevisiones() {
    String sql = """
        SELECT COUNT(*) AS total_pedidos,
               COALESCE(SUM(CASE WHEN revisado = TRUE THEN 1 ELSE 0 END), 0) AS total_revisados
        FROM pedido
        """;
    return jdbc.queryForObject(sql, Map.of(), (rs, rowNum) ->
        new ConteoRevisionProjection(rs.getLong("total_pedidos"), rs.getLong("total_revisados")));
  }

  private RevisionPedido mapear(ResultSet rs, int rowNum) throws SQLException {
    Timestamp fecha = rs.getTimestamp("fecha_revision");
    return new RevisionPedido(
        rs.getLong("id"),
        rs.getString("cliente"),
        rs.getString("producto_nombre"),
        rs.getBigDecimal("total"),
        rs.getBoolean("revisado") ? EstadoRevision.REVISADO : EstadoRevision.PENDIENTE,
        rs.getString("observacion"),
        fecha == null ? null : fecha.toLocalDateTime());
  }
}
