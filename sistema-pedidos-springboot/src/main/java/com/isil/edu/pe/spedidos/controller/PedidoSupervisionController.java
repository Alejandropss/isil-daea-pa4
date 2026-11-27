package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.exception.PedidoException;
import com.isil.edu.pe.spedidos.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.spedidos.exception.RevisionDuplicadaException;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import com.isil.edu.pe.spedidos.service.domain.RevisionPedido;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * PA4: pantalla de supervisión. Recibe parámetros, llama al Service o al cliente HTTP y arma el Model.
 * No ejecuta SQL.
 */
@Controller
@RequestMapping("/supervision/pedidos")
public class PedidoSupervisionController {

  private static final Set<String> ESTADOS = Set.of("", "PENDIENTE", "REVISADO");

  private final PedidoRevisionService revisionService;
  private final PedidoReporteClient reporteClient;

  public PedidoSupervisionController(PedidoRevisionService revisionService, PedidoReporteClient reporteClient) {
    this.revisionService = revisionService;
    this.reporteClient = reporteClient;
  }

  @GetMapping
  public String verSupervision(@RequestParam(name = "estado", defaultValue = "") String estado,
      Model model, HttpServletResponse response) {
    String filtro = estado.trim().toUpperCase();
    if (!ESTADOS.contains(filtro)) {
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      model.addAttribute("error", "El estado debe ser PENDIENTE o REVISADO.");
      filtro = "";
    }
    cargarVista(model, filtro);
    return "supervision-pedidos";
  }

  @PostMapping("/{id}/revisar")
  public String revisar(@PathVariable String id, @RequestParam(defaultValue = "") String observacion,
      Model model, HttpServletResponse response, RedirectAttributes redirect) {
    try {
      RevisionPedido revision = revisionService.revisar(convertirId(id), observacion);
      redirect.addFlashAttribute("mensaje", "Pedido #" + revision.pedidoId() + " revisado correctamente.");
      // Post/Redirect/Get: si el usuario recarga la página, no se envía otra revisión.
      return "redirect:/supervision/pedidos";
    } catch (PedidoNoEncontradoException e) {
      return mostrarError(HttpStatus.NOT_FOUND, e, id, observacion, model, response);
    } catch (RevisionDuplicadaException e) {
      return mostrarError(HttpStatus.CONFLICT, e, id, observacion, model, response);
    } catch (PedidoException e) {
      return mostrarError(HttpStatus.BAD_REQUEST, e, id, observacion, model, response);
    }
  }

  private String mostrarError(HttpStatus status, PedidoException e, String id, String observacion,
      Model model, HttpServletResponse response) {
    response.setStatus(status.value());
    model.addAttribute("error", e.getMessage());
    model.addAttribute("pedidoConError", id);
    model.addAttribute("observacionIngresada", observacion);
    cargarVista(model, "");
    return "supervision-pedidos";
  }

  // La lista local siempre se muestra; el resumen y las revisiones vienen del servicio de reportes.
  private void cargarVista(Model model, String estado) {
    model.addAttribute("pedidos", revisionService.listarParaSupervision());
    model.addAttribute("estado", estado);
    try {
      model.addAttribute("estadisticas", reporteClient.obtenerEstadisticasRevision());
      model.addAttribute("revisiones", reporteClient.listarRevisiones(estado));
    } catch (PedidoReporteNoDisponibleException e) {
      model.addAttribute("errorReportes", e.getMessage());
    }
  }

  private static long convertirId(String id) {
    try {
      return Long.parseLong(id.trim());
    } catch (NumberFormatException e) {
      throw new PedidoException("El id del pedido debe ser un número.", e);
    }
  }
}
