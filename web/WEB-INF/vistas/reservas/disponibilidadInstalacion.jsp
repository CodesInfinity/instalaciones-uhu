<%-- 
    Document   : disponibilidadInstalacion.jsp
    Created on : 6 nov 2025
    Author     : agustinrodriguez
    Description: VISTA DE CALENDARIO Y DISPONIBILIDAD
                 Muestra una rejilla de horarios para una instalación específica.
                 Permite navegar por semanas y seleccionar franjas horarias.
                 Se conecta con 'disponibilidad.js' para la lógica asíncrona.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:if test="${empty espacio}">
    <div class="alert alert-danger">
        <strong><i class="fas fa-exclamation-triangle"></i> Error Crítico:</strong> 
        No se han recibido los datos de la instalación. Por favor, vuelva al listado.
    </div>
</c:if>

<div class="reservas-container" data-espacio-id="${espacio.id}">
    <div class="disponibilidad-page">

        <div class="disponibilidad-header">
            <div class="disponibilidad-header-info">
                <a href="${pageContext.request.contextPath}/instalaciones/detalle?id=${espacio.id}" 
                   class="btn-back">
                    <i class="fas fa-arrow-left"></i> Volver a detalles
                </a>
                <h2 class="disponibilidad-title">
                    <i class="fas fa-building"></i> ${espacio.nombre}
                </h2>
                <p class="disponibilidad-subtitle">
                    ${espacio.ubicacion} | ${espacio.tipo}
                </p>
            </div>
        </div>

        <div class="fecha-selector">
            <div class="fecha-selector-header">
                <h3><i class="fas fa-calendar-day"></i> Selecciona un Día</h3>
                <p class="info-text">Las reservas son de 1 hora y 30 minutos (8:30 - 20:30)</p>
            </div>

            <div class="fecha-selector-body">

                <div class="fecha-nav">
                    <button class="nav-btn" id="btnSemanaAnterior" type="button" title="Semana anterior">
                        <i class="fas fa-chevron-left"></i>
                    </button>

                    <span class="fecha-rango" id="rangoFechas">
                        ${rangoFechasLabel}
                    </span>

                    <button class="nav-btn" id="btnSemanaSiguiente" type="button" title="Semana siguiente">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>

                <div class="dias-container" id="diasContainer">
                    <c:forEach var="dia" items="${diasDisponibles}">
                        <c:choose>
                            <%-- Caso: Fin de semana (Deshabilitado visualmente) --%>
                            <c:when test="${dia.esFinDeSemana}">
                                <div class="dia-option disabled">
                                    <div class="dia-nombre">${dia.nombre}</div>
                                    <div class="dia-numero">${dia.numero}</div>
                                    <div class="dia-mes">${dia.mes}</div>
                                    <div class="dia-no-disponible">
                                        <i class="fas fa-ban"></i>
                                    </div>
                                </div>
                            </c:when>
                            
                            <%-- Caso: Día laborable (Clicable) --%>
                            <c:otherwise>
                                <a href="?espacioId=${espacio.id}&fecha=${dia.fechaStr}" 
                                   class="dia-option ${dia.fechaStr == fecha ? 'active' : ''}"
                                   data-fecha="${dia.fechaStr}">
                                    <div class="dia-nombre">${dia.nombre}</div>
                                    <div class="dia-numero">${dia.numero}</div>
                                    <div class="dia-mes">${dia.mes}</div>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>

            </div>
        </div>

        <div class="horarios-disponibles-section">
            <div class="horarios-header">
                <h3>
                    <i class="fas fa-clock"></i> 
                    <span id="fechaSeleccionadaTexto">Horarios Disponibles para ${fechaFormateada}</span>
                </h3>
            </div>

            <div id="horariosLoading" class="horarios-loading" style="display: none;">
                <div class="spinner"></div>
                <p>Consultando disponibilidad...</p>
            </div>

            <div class="horarios-grid" id="horariosGrid">
                <c:choose>
                    <%-- Caso A: No hay huecos libres --%>
                    <c:when test="${empty horariosConInfo}">
                        <div class="horarios-vacio">
                            <i class="fas fa-calendar-times"></i>
                            <h4>No hay horarios disponibles</h4>
                            <p>Todos los horarios están reservados para este día.</p>
                        </div>
                    </c:when>
                    
                    <%-- Caso B: Renderizado inicial de horarios --%>
                    <c:otherwise>
                        <c:forEach var="horario" items="${horariosConInfo}">
                            <a href="${pageContext.request.contextPath}/reservas/nueva?espacioId=${espacio.id}&fecha=${horario.fecha}&hora=${horario.inicio}" 
                               class="horario-card">
                                <div class="horario-icon">
                                    <i class="fas fa-clock"></i>
                                </div>
                                <div class="horario-info">
                                    <div class="horario-time">
                                        ${horario.inicio} - ${horario.fin}
                                    </div>
                                    <div class="horario-duracion">
                                        1 hora 30 minutos
                                    </div>
                                </div>
                                <div class="horario-action">
                                    <i class="fas fa-arrow-right"></i>
                                </div>
                            </a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="info-adicional">
                <div class="info-box">
                    <i class="fas fa-info-circle"></i>
                    <div>
                        <strong>Información Importante:</strong>
                        <p>• Las reservas son de bloque fijo (90 min).</p>
                        <p>• Horario de apertura: 08:30 a 20:30.</p>
                        <p>• Cancelaciones permitidas hasta 24h antes.</p>
                    </div>
                </div>

                <div class="acciones-disponibilidad">
                    <a href="${pageContext.request.contextPath}/instalaciones/" class="btn btn-outline">
                        <i class="fas fa-th"></i> Ver todas las instalaciones
                    </a>
                    <a href="${pageContext.request.contextPath}/reservas/" class="btn btn-outline">
                        <i class="fas fa-list"></i> Mis Reservas
                    </a>
                </div>
            </div>
        </div>

    </div>
</div>

<script>
    window.ReservaConfig = {
        contextPath: '${pageContext.request.contextPath}',
        espacioId: '${espacio.id}',
        fechaActual: '${fecha}',
        inicioSemana: '${inicioSemana}'
    };
</script>

<script src="${pageContext.request.contextPath}/scripts/disponibilidad.js"></script>