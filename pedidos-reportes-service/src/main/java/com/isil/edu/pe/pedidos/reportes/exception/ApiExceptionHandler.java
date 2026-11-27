package com.isil.edu.pe.pedidos.reportes.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ProblemDetail pedidoNoEncontrado(PedidoNoEncontradoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Pedido no encontrado");
    return problem;
  }

  /** PA4: estado distinto de PENDIENTE o REVISADO. */
  @ExceptionHandler(EstadoRevisionInvalidoException.class)
  public ProblemDetail estadoInvalido(EstadoRevisionInvalidoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setTitle("Parámetro inválido");
    return problem;
  }

  /** PA4: la base no responde; se informa sin mostrar la traza técnica. */
  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail baseNoDisponible(DataAccessException e) {
    log.error("Error al consultar la base de datos.", e);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
        "La base de datos de reportes no está disponible.");
    problem.setTitle("Servicio no disponible");
    return problem;
  }
}