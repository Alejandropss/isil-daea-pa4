package com.isil.edu.pe.spedidos.exception;

/** El pedido ya fue revisado (se responde 409). */
public class RevisionDuplicadaException extends PedidoException {

  public RevisionDuplicadaException(long pedidoId) {
    super("El pedido " + pedidoId + " ya fue revisado; no se registra una segunda revisión.");
  }
}
