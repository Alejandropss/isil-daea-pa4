package com.isil.edu.pe.pedidos.reportes.service.impl;

import com.isil.edu.pe.pedidos.reportes.repository.RevisionReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ConteoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadoRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.RevisionPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RevisionReporteServiceImpl implements RevisionReporteService {

  private final RevisionReporteJdbcRepository repository;

  public RevisionReporteServiceImpl(RevisionReporteJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<RevisionPedido> listarRevisiones(String estado) {
    return EstadoRevision.desde(estado)
        .map(repository::listarRevisionesPorEstado)
        .orElseGet(repository::listarRevisiones);
  }

  @Override
  public EstadisticasRevision obtenerEstadisticas() {
    ConteoRevisionProjection conteo = repository.contarRevisiones();
    long pendientes = conteo.totalPedidos() - conteo.totalRevisados();
    return new EstadisticasRevision(conteo.totalPedidos(), conteo.totalRevisados(), pendientes,
        porcentaje(conteo.totalRevisados(), conteo.totalPedidos()));
  }

  // Sin pedidos el porcentaje es 0.00 (no se divide entre cero). Se redondea a 2 decimales.
  private static BigDecimal porcentaje(long revisados, long total) {
    if (total == 0) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(revisados * 100L)
        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
  }
}
