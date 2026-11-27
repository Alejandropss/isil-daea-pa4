package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import java.math.BigDecimal;

public record EstadisticasRevisionDto(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {

  public static EstadisticasRevisionDto from(EstadisticasRevision estadisticas) {
    return new EstadisticasRevisionDto(
        estadisticas.totalPedidos(),
        estadisticas.totalRevisados(),
        estadisticas.totalPendientes(),
        estadisticas.porcentajeRevisado());
  }
}
