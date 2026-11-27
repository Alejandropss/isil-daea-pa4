package com.isil.edu.pe.pedidos.reportes.service;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevision;
import com.isil.edu.pe.pedidos.reportes.service.domain.RevisionPedido;
import java.util.List;

public interface RevisionReporteService {

  /** estado vacío = todos; PENDIENTE o REVISADO filtra; otro valor = 400. */
  List<RevisionPedido> listarRevisiones(String estado);

  EstadisticasRevision obtenerEstadisticas();
}
