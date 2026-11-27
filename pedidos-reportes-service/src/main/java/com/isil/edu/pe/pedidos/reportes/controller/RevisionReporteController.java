package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.controller.dto.EstadisticasRevisionDto;
import com.isil.edu.pe.pedidos.reportes.controller.dto.RevisionPedidoDto;
import com.isil.edu.pe.pedidos.reportes.service.RevisionReporteService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** PA4: dos recursos nuevos de revisión. Solo traduce HTTP/JSON; los cálculos están en el Service. */
@RestController
@RequestMapping("/api/v1/pedidos")
public class RevisionReporteController {

  private final RevisionReporteService revisionService;

  public RevisionReporteController(RevisionReporteService revisionService) {
    this.revisionService = revisionService;
  }

  @GetMapping(value = "/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<RevisionPedidoDto> listarRevisiones(@RequestParam(required = false) String estado) {
    return revisionService.listarRevisiones(estado)
        .stream()
        .map(RevisionPedidoDto::from)
        .toList();
  }

  @GetMapping(value = "/estadisticas/revisiones", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EstadisticasRevisionDto> obtenerEstadisticas() {
    return ResponseEntity.ok(EstadisticasRevisionDto.from(revisionService.obtenerEstadisticas()));
  }
}
