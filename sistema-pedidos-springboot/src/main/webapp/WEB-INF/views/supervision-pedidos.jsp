<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Spring MVC + Spring JDBC + Spring REST</p>
            <h1>Supervisión de pedidos</h1>
            <p class="subtitulo">
                Revisar un pedido actualiza la tabla pedido y registra la revisión en una sola transacción local.
            </p>
        </div>

        <div class="acciones-cabecera">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}" class="boton-link boton-link-secundario">
                <span aria-hidden="true">←</span>
                Volver a pedidos
            </a>
            <c:url var="logoutUrl" value="/logout"/>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <c:if test="${not empty mensaje}">
        <div class="alerta exito"><c:out value="${mensaje}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alerta error"><c:out value="${error}"/></div>
    </c:if>

    <section class="tarjeta">
        <h2>Resumen de revisiones</h2>
        <p class="nota">Dato obtenido de pedidos-reportes-service (puerto 8083).</p>
        <c:choose>
            <c:when test="${not empty errorReportes}">
                <div class="alerta aviso">
                    <c:out value="${errorReportes}"/> Puedes seguir revisando pedidos: la revisión solo usa la base local.
                </div>
            </c:when>
            <c:otherwise>
                <div class="resumen-revisiones">
                    <div><span>Pedidos</span><strong><c:out value="${estadisticas.totalPedidos}"/></strong></div>
                    <div><span>Revisados</span><strong><c:out value="${estadisticas.totalRevisados}"/></strong></div>
                    <div><span>Pendientes</span><strong><c:out value="${estadisticas.totalPendientes}"/></strong></div>
                    <div><span>% revisado</span>
                        <strong><fmt:formatNumber value="${estadisticas.porcentajeRevisado}"
                                                  minFractionDigits="2" maxFractionDigits="2"/> %</strong></div>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="tarjeta">
        <h2>Pedidos</h2>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Producto</th>
                    <th>Total</th>
                    <th>Estado</th>
                    <th>Acción</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.productoNombre}"/></td>
                        <td>S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>
                            <span class="estado ${pedido.revisado ? 'estado-revisado' : 'estado-pendiente'}">
                                    ${pedido.revisado ? 'REVISADO' : 'PENDIENTE'}
                            </span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${pedido.revisado}">—</c:when>
                                <c:when test="${pageContext.request.isUserInRole('OPERADOR')}">
                                    <c:url var="revisarUrl" value="/supervision/pedidos/${pedido.id}/revisar"/>
                                    <form method="post" action="${revisarUrl}" class="form-revisar">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input name="observacion" maxlength="200" required placeholder="Observación"
                                               value="${pedidoConError eq pedido.id.toString() ? fn:escapeXml(observacionIngresada) : ''}">
                                        <button type="submit">Revisar</button>
                                    </form>
                                </c:when>
                                <c:otherwise>Solo lectura</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pedidos}">
                    <tr><td colspan="6" class="sin-datos">Aún no hay pedidos.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

    <c:if test="${empty errorReportes}">
        <section class="tarjeta">
            <div class="titulo-tabla titulo-tabla-acciones">
                <div>
                    <h2>Revisiones registradas</h2>
                    <p>Listado de pedidos-reportes-service con observación y fecha.</p>
                </div>
                <c:url var="supervisionUrl" value="/supervision/pedidos"/>
                <form method="get" action="${supervisionUrl}" class="form-filtro">
                    <select name="estado">
                        <option value="" ${empty estado ? 'selected' : ''}>Todos</option>
                        <option value="PENDIENTE" ${estado eq 'PENDIENTE' ? 'selected' : ''}>Pendientes</option>
                        <option value="REVISADO" ${estado eq 'REVISADO' ? 'selected' : ''}>Revisados</option>
                    </select>
                    <button type="submit">Filtrar</button>
                </form>
            </div>
            <div class="tabla-scroll">
                <table>
                    <thead>
                    <tr><th>ID</th><th>Cliente</th><th>Estado</th><th>Observación</th><th>Fecha de revisión</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="revision" items="${revisiones}">
                        <tr>
                            <td><c:out value="${revision.pedidoId}"/></td>
                            <td><c:out value="${revision.cliente}"/></td>
                            <td><c:out value="${revision.estado}"/></td>
                            <td><c:out value="${revision.observacion}" default="—"/></td>
                            <td><c:out value="${revision.fechaRevision}" default="—"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty revisiones}">
                        <tr><td colspan="5" class="sin-datos">No hay pedidos con ese estado.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </c:if>
</div>
</body>
</html>
