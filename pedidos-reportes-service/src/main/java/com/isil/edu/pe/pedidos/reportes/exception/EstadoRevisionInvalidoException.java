package com.isil.edu.pe.pedidos.reportes.exception;

/** PA4: el estado pedido no es PENDIENTE ni REVISADO (se responde 400). */
public class EstadoRevisionInvalidoException extends RuntimeException {

  public EstadoRevisionInvalidoException(String estado) {
    super("El estado '" + estado + "' no es válido. Use PENDIENTE o REVISADO.");
  }
}
