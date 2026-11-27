package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;

/** DTO de transporte: lo que responde GET /api/v1/pedidos/estadisticas/revisiones. */
public record EstadisticasRevisionResponse(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) { }
