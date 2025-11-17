<%-- 
    Document   : disponibilidadInstalacion
    Created on : 14 nov 2025
    Author     : v0
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/reservas.css">

<div class="reservas-container">
    <div class="disponibilidad-page">
        
        <%-- Header de la instalación --%>
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

        <%-- Selector de fecha --%>
        <div class="fecha-selector">
            <div class="fecha-selector-header">
                <h3><i class="fas fa-calendar-day"></i> Selecciona un Día</h3>
                <p class="info-text">Las reservas son de 1 hora y 30 minutos (8:30 - 20:30)</p>
            </div>
            
            <div class="fecha-selector-body">
                <%-- Usar lista preparada desde el controlador --%>
                <c:forEach var="dia" items="${diasDisponibles}">
                    <c:choose>
                        <c:when test="${dia.esFinDeSemana}">
                            <div class="dia-option disabled" title="No hay reservas los fines de semana">
                                <div class="dia-nombre">
                                    ${dia.nombre}
                                </div>
                                <div class="dia-numero">
                                    ${dia.numero}
                                </div>
                                <div class="dia-mes">
                                    ${dia.mes}
                                </div>
                                <div class="dia-no-disponible">
                                    <i class="fas fa-ban"></i>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/reservas/disponibilidad?espacioId=${espacio.id}&fecha=${dia.fechaStr}" 
                               class="dia-option ${dia.activo ? 'active' : ''}">
                                <div class="dia-nombre">
                                    ${dia.nombre}
                                </div>
                                <div class="dia-numero">
                                    ${dia.numero}
                                </div>
                                <div class="dia-mes">
                                    ${dia.mes}
                                </div>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </div>

        <%-- Horarios disponibles --%>
        <div class="horarios-disponibles-section">
            <div class="horarios-header">
                <h3>
                    <i class="fas fa-clock"></i> 
                    <%-- Usar fecha formateada desde el controlador --%>
                    Horarios Disponibles para ${fechaFormateada}
                </h3>
                <c:if test="${empty horariosConInfo}">
                    <p class="info-text">No hay horarios disponibles para este día</p>
                </c:if>
            </div>

            <div class="horarios-grid">
                <%-- Usar lista de horarios con información preparada --%>
                <c:if test="${empty horariosConInfo}">
                    <div class="horarios-vacio">
                        <i class="fas fa-calendar-times"></i>
                        <h4>No hay horarios disponibles</h4>
                        <p>Todos los horarios están reservados para este día. Intenta con otra fecha.</p>
                    </div>
                </c:if>

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
            </div>

            <div class="info-adicional">
                <div class="info-box">
                    <i class="fas fa-info-circle"></i>
                    <div>
                        <strong>Información Importante:</strong>
                        <p>• Las reservas son de 1 hora y 30 minutos de duración</p>
                        <p>• Horario disponible: de 8:30 a 20:30</p>
                        <p>• Primera reserva: 8:30 - 10:00</p>
                        <p>• Última reserva: 19:00 - 20:30</p>
                        <p>• No se permiten reservas los fines de semana</p>
                        <p>• Al seleccionar un horario serás dirigido al formulario de reserva</p>
                    </div>
                </div>
                
                <div class="acciones-disponibilidad">
                    <a href="${pageContext.request.contextPath}/instalaciones/" 
                       class="btn btn-outline">
                        <i class="fas fa-th"></i> Ver Todas las Instalaciones
                    </a>
                    <a href="${pageContext.request.contextPath}/reservas/" 
                       class="btn btn-outline">
                        <i class="fas fa-list"></i> Mis Reservas
                    </a>
                </div>
            </div>
        </div>

    </div>
</div>