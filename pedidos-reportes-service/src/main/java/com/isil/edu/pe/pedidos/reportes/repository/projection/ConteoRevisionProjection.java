package com.isil.edu.pe.pedidos.reportes.repository.projection;

/** PA4: conteos que devuelve el SQL; los porcentajes se calculan en el Service. */
public record ConteoRevisionProjection(long totalPedidos, long totalRevisados) {
}
