<%-- 
    Document   : detalleInstalacion.jsp
    Created on : 6 nov 2025, 20:18:04
    Author     : agustinrodriguez
    Description: VISTA DE DETALLE DE INSTALACIÓN
                 Muestra información completa, mapa, características y opciones de reserva.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="instalaciones-container">
    <div class="detalle-container">
        <div class="detalle-instalacion">
            
            <c:choose>
                <%-- CASO A: Imagen Disponible --%>
                <c:when test="${not empty instalacion.imagenUrl}">
                    <div class="detalle-header-with-image">
                        <img src="${pageContext.request.contextPath}${instalacion.imagenUrl}" 
                             alt="${instalacion.nombre}" 
                             class="detalle-header-image"
                             onerror="this.style.display='none'; this.parentElement.classList.add('detalle-header'); this.parentElement.classList.remove('detalle-header-with-image');">

                        <div class="detalle-header-overlay">
                            <div class="header-actions">
                                <a href="${pageContext.request.contextPath}/instalaciones/" class="btn btn-secondary">
                                    <i class="fas fa-arrow-left"></i> Volver
                                </a>
                                <%-- Botón Admin: Editar --%>
                                <c:if test="${sessionScope.usuario.rol == 0}">
                                    <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" class="btn btn-primary">
                                        <i class="fas fa-edit"></i> Editar
                                    </a>
                                </c:if>
                            </div>

                            <h1 class="detalle-title-overlay">
                                ${instalacion.nombre}
                            </h1>

                            <span class="badge-overlay badge-large">
                                <c:choose>
                                    <c:when test="${fn:containsIgnoreCase(instalacion.tipo, 'Fútbol')}"><i class="fas fa-futbol"></i></c:when>
                                    <c:when test="${fn:containsIgnoreCase(instalacion.tipo, 'Baloncesto')}"><i class="fas fa-basketball-ball"></i></c:when>
                                    <c:when test="${fn:containsIgnoreCase(instalacion.tipo, 'Tenis')}"><i class="fas fa-table-tennis"></i></c:when>
                                    <c:when test="${fn:containsIgnoreCase(instalacion.tipo, 'Natación')}"><i class="fas fa-swimmer"></i></c:when>
                                    <c:when test="${fn:containsIgnoreCase(instalacion.tipo, 'Gimnasio')}"><i class="fas fa-dumbbell"></i></c:when>
                                    <c:otherwise><i class="fas fa-map-marker-alt"></i></c:otherwise>
                                </c:choose>
                                ${instalacion.tipo}
                            </span>
                        </div>
                    </div>
                </c:when>

                <%-- CASO B: Sin Imagen (Diseño Plano) --%>
                <c:otherwise>
                    <div class="detalle-header">
                        <div class="header-actions">
                            <a href="${pageContext.request.contextPath}/instalaciones/" class="btn btn-secondary">
                                <i class="fas fa-arrow-left"></i> Volver
                            </a>
                            <c:if test="${sessionScope.usuario.rol == 0}">
                                <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" class="btn btn-primary">
                                    <i class="fas fa-edit"></i> Editar
                                </a>
                            </c:if>
                        </div>
                        <h1 class="detalle-title">
                            <i class="fas fa-building"></i> ${instalacion.nombre}
                        </h1>
                        <span class="badge badge-large">
                            <i class="fas fa-info-circle"></i> ${instalacion.tipo}
                        </span>
                    </div>
                </c:otherwise>
            </c:choose>
            
            <div class="detalle-body">
                <div class="detalle-content">
                    
                    <div class="detalle-info">
                        
                        <div class="detalle-info-item">
                            <div class="info-icon"><i class="fas fa-map-marker-alt"></i></div>
                            <div class="info-content">
                                <h4>Ubicación</h4>
                                <p>${instalacion.ubicacion}</p>
                            </div>
                        </div>
                        
                        <div class="detalle-info-item">
                            <div class="info-icon"><i class="fas fa-info-circle"></i></div>
                            <div class="info-content">
                                <h4>Estado</h4>
                                <span class="estado-disponible">
                                    <i class="fas fa-check-circle"></i> Disponible
                                </span>
                            </div>
                        </div>
                        
                        <div class="detalle-info-item full-width">
                            <div class="info-icon"><i class="fas fa-align-left"></i></div>
                            <div class="info-content">
                                <h4>Descripción</h4>
                                <p>${not empty instalacion.descripcion ? instalacion.descripcion : 'No hay descripción disponible.'}</p>
                            </div>
                        </div>

                        <div class="detalle-info-item">
                            <div class="info-icon"><i class="fas fa-clock"></i></div>
                            <div class="info-content">
                                <h4>Horario de Apertura</h4>
                                <p>Lunes a Viernes: 08:30 - 21:00<br>Sábados: Cerrado</p>
                            </div>
                        </div>
                        
                        <div class="detalle-info-item full-width">
                            <div class="info-icon"><i class="fas fa-calendar-check"></i></div>
                            <div class="info-content">
                                <h4>Reservar Instalación</h4>
                                <p class="subtitle-disponibilidad">Consulta disponibilidad en tiempo real.</p>
                                
                                <c:choose>
                                    <%-- Usuario Logueado: Botones de acción --%>
                                    <c:when test="${not empty sessionScope.usuario}">
                                        <div class="boton-disponibilidad-container">
                                            <a href="${pageContext.request.contextPath}/reservas/disponibilidad?espacioId=${instalacion.id}" class="btn btn-primary btn-large">
                                                <i class="fas fa-calendar-day"></i> Ver Disponibilidad
                                            </a>
                                            <a href="${pageContext.request.contextPath}/reservas/nueva?espacioId=${instalacion.id}" class="btn btn-outline btn-large">
                                                <i class="fas fa-plus-circle"></i> Nueva Reserva
                                            </a>
                                        </div>
                                    </c:when>
                                    <%-- Usuario Anónimo: Aviso --%>
                                    <c:otherwise>
                                        <p class="info-login">
                                            <i class="fas fa-lock"></i>
                                            Debes <a href="${pageContext.request.contextPath}/usuario/login">iniciar sesión</a> 
                                            para realizar reservas.
                                        </p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                    
                    <div class="detalle-sidebar">
                        <div class="sidebar-card">
                            <div class="sidebar-icon"><i class="fas fa-shield-alt"></i></div>
                            <h4>Normativa de Uso</h4>
                            <p class="sidebar-text">
                                Es obligatorio el uso de ropa deportiva adecuada y calzado específico para la superficie.
                            </p>
                        </div>

                        <c:if test="${sessionScope.usuario.rol == 0}">
                            <div class="sidebar-card">
                                <h4>Administración</h4>
                                <div class="sidebar-actions">
                                    <a href="${pageContext.request.contextPath}/instalaciones/borrar?id=${instalacion.id}" 
                                       class="btn btn-danger btn-block"
                                       onclick="return confirm('¿Eliminar esta instalación permanentemente?')">
                                        <i class="fas fa-trash"></i> Eliminar
                                    </a>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${sessionScope.usuario.rol == 0}">
            <div class="admin-actions-panel">
                <div class="panel-header">
                    <h3><i class="fas fa-cogs"></i> Acciones Administrativas</h3>
                </div>
                <div class="panel-buttons">
                    <a href="${pageContext.request.contextPath}/instalaciones/panel" class="btn btn-outline">
                        <i class="fas fa-list"></i> Listado Instalaciones
                    </a>
                    <a href="${pageContext.request.contextPath}/reservas/panel" class="btn btn-outline">
                        <i class="fas fa-calendar-alt"></i> Gestión Reservas
                    </a>
                </div>
            </div>
        </c:if>
    </div>
</div>