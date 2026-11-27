package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** PA4: pedido con su estado de revisión; observación y fecha solo si ya fue revisado. */
public record RevisionPedido(
    long pedidoId,
    String cliente,
    String productoNombre,
    BigDecimal total,
    EstadoRevision estado,
    String observacion,
    LocalDateTime fechaRevision
) { }
