package com.isil.edu.pe.spedidos.service.domain;

import java.time.LocalDateTime;

/** Resultado de una revisión confirmada. */
public record RevisionPedido(long pedidoId, String observacion, LocalDateTime fechaRevision) {
}
