package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.service.domain.PedidoSupervision;
import com.isil.edu.pe.spedidos.service.domain.RevisionPedido;
import java.util.List;

public interface PedidoRevisionService {

  /** Marca el pedido como revisado y registra la revisión en una sola transacción. */
  RevisionPedido revisar(long pedidoId, String observacion);

  List<PedidoSupervision> listarParaSupervision();
}
