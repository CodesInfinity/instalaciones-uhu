<%-- 
    Document   : listaReservas
    Created on : 13 nov 2025
    Author     : agustinrodriguez
    
    Vista para mostrar las reservas del usuario
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="panel-content">
    <div class="table-container">
        <div class="table-header">
            <div class="header-title-with-button">
                <h2>Mis Reservas</h2>
                <a href="${pageContext.request.contextPath}/reservas/nueva" class="btn-solicitudes-panel">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    Nueva Reserva
                </a>
            </div>
            <div class="table-stats">
                <span class="stat-badge">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    ${reservas.size()} reservas
                </span>
            </div>
        </div>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                ${param.success}
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty reservas}">
                <div class="reservas-empty">
                    <div class="reservas-empty-icon">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                    </div>
                    <h3>No tienes reservas</h3>
                    <p>Comienza a reservar instalaciones deportivas para tus actividades</p>
                    <a href="${pageContext.request.contextPath}/reservas/nueva" class="btn-primary">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                        </svg>
                        Crear Primera Reserva
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="reservas-container">
                    <div class="reservas-grid">
                        <c:forEach var="reserva" items="${reservas}">
                            <jsp:useBean id="now" class="java.util.Date" />
                            <c:set var="ahora" value="${now.time}" />
                            <c:set var="inicioReserva" value="${reserva.inicioDate.time}" />
                            
                            <div class="reserva-card">
                                <div class="reserva-card-header">
                                    <div class="reserva-espacio-info">
                                        <h3 class="reserva-espacio-nombre">${reserva.espacio.nombre}</h3>
                                        <div class="reserva-espacio-tipo">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                                            </svg>
                                            ${reserva.espacio.ubicacion}
                                        </div>
                                    </div>
                                </div>

                                <div class="reserva-card-body">
                                    <div class="reserva-info-item">
                                        <div class="reserva-info-icon">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                                            </svg>
                                        </div>
                                        <div class="reserva-info-content">
                                            <div class="reserva-info-label">Fecha</div>
                                            <div class="reserva-info-value">
                                                <fmt:formatDate value="${reserva.inicioDate}" pattern="dd/MM/yyyy"/>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="reserva-info-item">
                                        <div class="reserva-info-icon">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                            </svg>
                                        </div>
                                        <div class="reserva-info-content">
                                            <div class="reserva-info-label">Horario</div>
                                            <div class="reserva-info-value">
                                                <fmt:formatDate value="${reserva.inicioDate}" pattern="HH:mm"/> - 
                                                <fmt:formatDate value="${reserva.finDate}" pattern="HH:mm"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <c:if test="${sessionScope.usuario.rol == 0}">
                                    <div class="reserva-card-footer">
                                        <div class="reserva-usuario-info">
                                            <div class="reserva-usuario-avatar">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                                </svg>
                                            </div>
                                            <span class="reserva-usuario-nombre">${reserva.usuario.nombre}</span>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>