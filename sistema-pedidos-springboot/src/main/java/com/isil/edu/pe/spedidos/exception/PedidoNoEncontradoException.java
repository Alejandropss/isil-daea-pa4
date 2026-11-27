package com.isil.edu.pe.spedidos.exception;

/** El pedido no existe (se responde 404). */
public class PedidoNoEncontradoException extends PedidoException {

  public PedidoNoEncontradoException(long pedidoId) {
    super("No existe un pedido con id " + pedidoId + ".");
  }
}
