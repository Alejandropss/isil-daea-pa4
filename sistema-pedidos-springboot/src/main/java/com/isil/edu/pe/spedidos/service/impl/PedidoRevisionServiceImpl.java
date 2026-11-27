package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.entity.PedidoRevision;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.exception.RevisionDuplicadaException;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import com.isil.edu.pe.spedidos.service.domain.PedidoSupervision;
import com.isil.edu.pe.spedidos.service.domain.RevisionPedido;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

  private final PedidoRevisionJdbcRepository repository;

  public PedidoRevisionServiceImpl(PedidoRevisionJdbcRepository repository) {
    this.repository = repository;
  }

  /**
   * Las dos escrituras (UPDATE pedido e INSERT pedido_revision) van en la misma transacción local de MySQL:
   * si cualquiera lanza una excepción, se revierten las dos. Aquí no se llama a ningún servicio HTTP.
   */
  @Override
  @Transactional
  public RevisionPedido revisar(long pedidoId, String observacion) {
    String texto = validar(pedidoId, observacion);

    boolean yaRevisado = repository.buscarEstadoRevision(pedidoId)
        .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
    if (yaRevisado) {
      throw new RevisionDuplicadaException(pedidoId);
    }

    // Si otra sesión lo revisó un instante antes, el UPDATE no cambia ninguna fila.
    if (repository.marcarRevisado(pedidoId) != 1) {
      throw new RevisionDuplicadaException(pedidoId);
    }

    LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    try {
      repository.insertarRevision(pedidoId, texto, fecha);
    } catch (DuplicateKeyException e) {
      // El UNIQUE de pedido_id es la última barrera; la excepción sigue y provoca rollback.
      throw new RevisionDuplicadaException(pedidoId);
    }
    return new RevisionPedido(pedidoId, texto, fecha);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PedidoSupervision> listarParaSupervision() {
    return repository.listarParaSupervision();
  }

  private static String validar(long pedidoId, String observacion) {
    if (pedidoId <= 0) {
      throw new PedidoException("El id del pedido debe ser mayor que cero.");
    }
    if (observacion == null || observacion.isBlank()) {
      throw new PedidoException("La observación es obligatoria.");
    }
    String texto = observacion.trim();
    if (texto.length() > PedidoRevision.OBSERVACION_MAXIMA) {
      throw new PedidoException(
          "La observación no puede superar " + PedidoRevision.OBSERVACION_MAXIMA + " caracteres.");
    }
    return texto;
  }
}
