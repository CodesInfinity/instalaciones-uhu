<%-- 
    Document   : detalleInstalacion
    Created on : 6 nov 2025, 20:18:04
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="instalaciones-container">
    <div class="detalle-container">
        <div class="detalle-instalacion">
            
            <%-- INICIO DEL BLOQUE DE CABECERA CONDICIONAL --%>
            <c:choose>
                <%-- CASO 1: La instalación SÍ tiene una imagen --%>
                <c:when test="${not empty instalacion.imagenUrl}">
                    <div class="detalle-header-with-image">
                        <img src="${pageContext.request.contextPath}${instalacion.imagenUrl}" 
                             alt="${instalacion.nombre}" 
                             class="detalle-header-image"
                             onerror="this.style.display='none'; this.parentElement.classList.add('detalle-header'); this.parentElement.classList.remove('detalle-header-with-image');">

                        <div class="detalle-header-overlay">
                            
                            <div class="header-actions">
                                <a href="${pageContext.request.contextPath}/instalaciones/" 
                                   class="btn btn-secondary">
                                    <i class="fas fa-arrow-left"></i> Volver
                                </a>
                                <c:if test="${sessionScope.usuario.rol == 0}">
                                    <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" 
                                       class="btn btn-primary">
                                        <i class="fas fa-edit"></i> Editar
                                    </a>
                                </c:if>
                            </div>

                            <h1 class="detalle-title-overlay">
                                ${instalacion.nombre}
                            </h1>

                            <span class="badge-overlay badge-large">
                                <c:choose>
                                    <c:when test="${instalacion.tipo == 'Fútbol'}">
                                        <i class="fas fa-futbol"></i>
                                    </c:when>
                                    <c:when test="${instalacion.tipo == 'Baloncesto'}">
                                        <i class="fas fa-basketball-ball"></i>
                                    </c:when>
                                    <c:when test="${instalacion.tipo == 'Tenis'}">
                                        <i class="fas fa-table-tennis"></i>
                                    </c:when>
                                    <c:when test="${instalacion.tipo == 'Natación'}">
                                        <i class="fas fa-swimmer"></i>
                                    </c:when>
                                    <c:when test="${instalacion.tipo == 'Gimnasio'}">
                                        <i class="fas fa-dumbbell"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fas fa-map-marker-alt"></i>
                                    </c:otherwise>
                                </c:choose>
                                ${instalacion.tipo}
                            </span>
                        </div>
                    </div>
                </c:when>

                <%-- CASO 2: La instalación NO tiene imagen (usa la cabecera roja) --%>
                <c:otherwise>
                    <div class="detalle-header">
                        <div class="header-actions">
                            <a href="${pageContext.request.contextPath}/instalaciones/" 
                               class="btn btn-secondary">
                                <i class="fas fa-arrow-left"></i> Volver
                            </a>
                            <c:if test="${sessionScope.usuario.rol == 0}">
                                <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" 
                                   class="btn btn-primary">
                                    <i class="fas fa-edit"></i> Editar
                                </a>
                            </c:if>
                        </div>
                        <h1 class="detalle-title">
                            <i class="fas fa-building"></i> ${instalacion.nombre}
                        </h1>
                        <span class="badge badge-${instalacion.tipo.toLowerCase()} badge-large">
                            <c:choose>
                                <c:when test="${instalacion.tipo == 'Fútbol'}">
                                    <i class="fas fa-futbol"></i>
                                </c:when>
                                <c:when test="${instalacion.tipo == 'Baloncesto'}">
                                    <i class="fas fa-basketball-ball"></i>
                                </c:when>
                                <c:when test="${instalacion.tipo == 'Tenis'}">
                                    <i class="fas fa-table-tennis"></i>
                                </c:when>
                                <c:when test="${instalacion.tipo == 'Natación'}">
                                    <i class="fas fa-swimmer"></i>
                                </c:when>
                                <c:when test="${instalacion.tipo == 'Gimnasio'}">
                                    <i class="fas fa-dumbbell"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="fas fa-map-marker-alt"></i>
                                </c:otherwise>
                            </c:choose>
                            ${instalacion.tipo}
                        </span>
                    </div>
                </c:otherwise>
            </c:choose>
            <%-- FIN DEL BLOQUE DE CABECERA CONDICIONAL --%>
            
            <div class="detalle-body">
                <div class="detalle-content">
                    <div class="detalle-info">
                        <div class="detalle-info-item">
                            <div class="info-icon">
                                <i class="fas fa-map-marker-alt"></i>
                            </div>
                            <div class="info-content">
                                <h4>Ubicación</h4>
                                <p>${instalacion.ubicacion}</p>
                            </div>
                        </div>
                        
                        <div class="detalle-info-item">
                            <div class="info-icon">
                                <i class="fas fa-info-circle"></i>
                            </div>
                            <div class="info-content">
                                <h4>Estado</h4>
                                <span class="estado-disponible">
                                    <i class="fas fa-check-circle"></i> Disponible
                                </span>
                            </div>
                        </div>
                        
                        <div class="detalle-info-item full-width">
                            <div class="info-icon">
                                <i class="fas fa-align-left"></i>
                            </div>
                            <div class="info-content">
                                <h4>Descripción</h4>
                                <p>${not empty instalacion.descripcion ? instalacion.descripcion : 'No hay descripción disponible.'}</p>
                            </div>
                        </div>

                        <div class="detalle-info-item">
                            <div class="info-icon">
                                <i class="fas fa-calendar-alt"></i>
                            </div>
                            <div class="info-content">
                                <h4>Disponibilidad</h4>
                                <p>Lunes a Viernes: 7:00 - 22:00<br>Sábados: 8:00 - 20:00</p>
                            </div>
                        </div>
                    </div>
                    
                    <div class="detalle-sidebar">
                        <div class="sidebar-card">
                            <div class="sidebar-icon">
                                <i class="fas fa-building"></i>
                            </div>
                            <h4>Información Adicional</h4>
                            <p class="sidebar-text">
                                Instalación registrada en el sistema de gestión deportiva de la UHU.
                            </p>
                            
                            <c:if test="${sessionScope.usuario.rol == 0}">
                                <div class="sidebar-actions">
                                    <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" 
                                       class="btn btn-primary btn-block">
                                        <i class="fas fa-edit"></i> Editar Instalación
                                    </a>
                                    <a href="${pageContext.request.contextPath}/instalaciones/borrar?id=${instalacion.id}" 
                                       class="btn btn-danger btn-block"
                                       onclick="return confirm('¿Está seguro de eliminar \\'${instalacion.nombre}\\'?')">
                                        <i class="fas fa-trash"></i> Eliminar
                                    </a>
                                </div>
                            </c:if>
                        </div>

                        <div class="sidebar-card">
                            <div class="sidebar-icon">
                                <i class="fas fa-clock"></i>
                            </div>
                            <h4>Horario Recomendado</h4>
                            <p class="sidebar-text">
                                <i class="fas fa-sun"></i> Mañanas: Menos concurrido<br>
                                <i class="fas fa-moon"></i> Tardes: Mayor actividad
                            </p>
                        </div>
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
                    <a href="${pageContext.request.contextPath}/instalaciones/panel" 
                       class="btn btn-outline">
                        <i class="fas fa-building"></i> Panel de Instalaciones
                    </a>
                    <a href="${pageContext.request.contextPath}/usuario/panel" 
                       class="btn btn-outline">
                        <i class="fas fa-users"></i> Panel de Usuarios
                    </a>
                    <a href="${pageContext.request.contextPath}/usuario/solicitudes" 
                       class="btn btn-outline">
                        <i class="fas fa-user-graduate"></i> Solicitudes
                    </a>
                </div>
            </div>
        </c:if>
    </div>
</div>