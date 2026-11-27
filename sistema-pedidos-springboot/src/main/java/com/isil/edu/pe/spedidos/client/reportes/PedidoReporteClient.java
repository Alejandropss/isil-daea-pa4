package com.isil.edu.pe.spedidos.client.reportes;

import com.isil.edu.pe.spedidos.client.reportes.dto.EstadisticasRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.RevisionPedidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import java.util.List;

public interface PedidoReporteClient {

  ResumenPedidosResponse obtenerResumen();

  List<PedidoReporteResponse> buscarPorCliente(String cliente);

  List<ProductoVendidoResponse> listarProductosMasVendidos();

  /** PA4: pedidos con su estado de revisión. estado vacío = todos; PENDIENTE o REVISADO filtra. */
  List<RevisionPedidoResponse> listarRevisiones(String estado);

  /** PA4: totales de revisión calculados por pedidos-reportes-service. */
  EstadisticasRevisionResponse obtenerEstadisticasRevision();

}
