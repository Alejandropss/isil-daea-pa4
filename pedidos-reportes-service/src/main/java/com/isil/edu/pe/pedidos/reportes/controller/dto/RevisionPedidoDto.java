package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.RevisionPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RevisionPedidoDto(
    long pedidoId,
    String cliente,
    String productoNombre,
    BigDecimal total,
    String estado,
    String observacion,
    LocalDateTime fechaRevision
) {

  public static RevisionPedidoDto from(RevisionPedido revision) {
    return new RevisionPedidoDto(
        revision.pedidoId(),
        revision.cliente(),
        revision.productoNombre(),
        revision.total(),
        revision.estado().name(),
        revision.observacion(),
        revision.fechaRevision());
  }
}
