package com.isil.edu.pe.pedidos.reportes.service.domain;

import com.isil.edu.pe.pedidos.reportes.exception.EstadoRevisionInvalidoException;
import java.util.Optional;

/** PA4: estados posibles de la revisión de un pedido. */
public enum EstadoRevision {
  PENDIENTE,
  REVISADO;

  /** Vacío = sin filtro. Acepta mayúsculas o minúsculas; cualquier otro valor es inválido. */
  public static Optional<EstadoRevision> desde(String valor) {
    if (valor == null || valor.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(valueOf(valor.trim().toUpperCase()));
    } catch (IllegalArgumentException e) {
      throw new EstadoRevisionInvalidoException(valor);
    }
  }
}
