package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;

/** PA4: resumen de revisiones calculado en el Service. */
public record EstadisticasRevision(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) { }
