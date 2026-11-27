package com.isil.edu.pe.spedidos.service.domain;

import java.math.BigDecimal;

/** Fila de la pantalla de supervisión. */
public record PedidoSupervision(long id, String cliente, String productoNombre, BigDecimal total, boolean revisado) {
}
