package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO de transporte: lo que responde GET /api/v1/pedidos/revisiones (no es una entidad JPA). */
public record RevisionPedidoResponse(
    long pedidoId,
    String cliente,
    String productoNombre,
    BigDecimal total,
    String estado,
    String observacion,
    LocalDateTime fechaRevision
) { }
