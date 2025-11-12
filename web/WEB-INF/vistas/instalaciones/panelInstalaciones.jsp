<%-- 
    Document   : panelInstalaciones
    Created on : 6 nov 2025, 20:17:31
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="instalaciones-container">
    <div class="instalaciones-header">
        <div class="header-content">
            <span class="badge">Panel Administrativo</span>
            <h1 class="page-title">Gestión de Instalaciones</h1>
            <p class="page-subtitle">Administra todas las instalaciones deportivas del sistema</p>
        </div>
        <a href="${pageContext.request.contextPath}/instalaciones/nueva" class="btn btn-primary">
            <i class="fas fa-plus-circle"></i> Nueva Instalación
        </a>
    </div>

    <!-- Estadísticas -->
    <div class="stats-grid">
        <div class="stats-card stats-primary">
            <div class="stats-content">
                <div class="stats-info">
                    <h3 class="stats-number">${instalaciones.size()}</h3>
                    <p class="stats-label">Total Instalaciones</p>
                </div>
                <div class="stats-icon">
                    <i class="fas fa-building"></i>
                </div>
            </div>
        </div>
        
        <div class="stats-card stats-success">
            <div class="stats-content">
                <div class="stats-info">
                    <h3 class="stats-number">
                        <c:set var="canchasCount" value="0" />
                        <c:forEach var="inst" items="${instalaciones}">
                            <c:if test="${inst.tipo.toLowerCase().contains('futbol') || inst.tipo.toLowerCase().contains('tenis') || inst.tipo.toLowerCase().contains('pádel')}">
                                <c:set var="canchasCount" value="${canchasCount + 1}" />
                            </c:if>
                        </c:forEach>
                        ${canchasCount}
                    </h3>
                    <p class="stats-label">Canchas</p>
                </div>
                <div class="stats-icon">
                    <i class="fas fa-futbol"></i>
                </div>
            </div>
        </div>
        
        <div class="stats-card stats-info">
            <div class="stats-content">
                <div class="stats-info">
                    <h3 class="stats-number">
                        <c:set var="gimnasiosCount" value="0" />
                        <c:forEach var="inst" items="${instalaciones}">
                            <c:if test="${inst.tipo.toLowerCase().contains('gimnasio') || inst.tipo.toLowerCase().contains('fitness')}">
                                <c:set var="gimnasiosCount" value="${gimnasiosCount + 1}" />
                            </c:if>
                        </c:forEach>
                        ${gimnasiosCount}
                    </h3>
                    <p class="stats-label">Gimnasios</p>
                </div>
                <div class="stats-icon">
                    <i class="fas fa-dumbbell"></i>
                </div>
            </div>
        </div>
    </div>

    <!-- Tabla de Instalaciones -->
    <div class="table-container">
        <div class="table-header">
            <h3><i class="fas fa-list"></i> Lista de Instalaciones</h3>
            <div class="table-stats">
                <span class="stat-badge">
                    <i class="fas fa-building"></i> ${instalaciones.size()} instalaciones
                </span>
            </div>
        </div>
        <div class="table-content">
            <c:choose>
                <c:when test="${not empty instalaciones}">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th><i class="fas fa-signature"></i> Nombre</th>
                                <th><i class="fas fa-tag"></i> Tipo</th>
                                <th><i class="fas fa-map-marker-alt"></i> Ubicación</th>
                                <th><i class="fas fa-align-left"></i> Descripción</th>
                                <th class="actions-column"><i class="fas fa-cogs"></i> Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="instalacion" items="${instalaciones}">
                                <tr>
                                    <td class="nombre-cell">
                                        <i class="fas fa-building text-muted"></i> 
                                        <strong>${instalacion.nombre}</strong>
                                    </td>
                                    <td>
                                        <span class="badge badge-${instalacion.tipo.toLowerCase()}">
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
                                    </td>
                                    <td>
                                        <i class="fas fa-map-marker-alt text-danger"></i>
                                        ${instalacion.ubicacion}
                                    </td>
                                    <td class="descripcion-cell">
                                        <c:choose>
                                            <c:when test="${not empty instalacion.descripcion && instalacion.descripcion.length() > 50}">
                                                <i class="fas fa-align-left text-muted"></i>
                                                ${instalacion.descripcion.substring(0, 50)}...
                                            </c:when>
                                            <c:otherwise>
                                                <i class="fas fa-align-left text-muted"></i>
                                                ${instalacion.descripcion}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="actions-cell">
                                        <div class="action-buttons">
                                            <a href="${pageContext.request.contextPath}/instalaciones/detalle?id=${instalacion.id}" 
                                               class="btn-icon btn-info" title="Ver detalles">
                                                <i class="fas fa-eye"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/instalaciones/editar?id=${instalacion.id}" 
                                               class="btn-icon btn-warning" title="Editar instalación">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/instalaciones/borrar?id=${instalacion.id}" 
                                               class="btn-icon btn-danger" 
                                               title="Eliminar instalación"
                                               onclick="return confirm('¿Estás seguro de eliminar \\'${instalacion.nombre}\\'?')">
                                                <i class="fas fa-trash"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="empty-state">
                        <div class="empty-icon">
                            <i class="fas fa-building"></i>
                        </div>
                        <h3>No hay instalaciones registradas</h3>
                        <p>Comienza agregando la primera instalación deportiva.</p>
                        <a href="${pageContext.request.contextPath}/instalaciones/nueva" class="btn btn-primary">
                            <i class="fas fa-plus-circle"></i> Agregar Primera Instalación
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Navegación rápida -->
    <div class="quick-nav">
        <div class="quick-nav-header">
            <h3><i class="fas fa-compass"></i> Navegación Rápida</h3>
        </div>
        <div class="quick-nav-buttons">
            <a href="${pageContext.request.contextPath}/usuario/panel" class="btn btn-outline">
                <i class="fas fa-users"></i> Panel de usuarios
            </a>
            <a href="${pageContext.request.contextPath}/usuario/solicitudes" class="btn btn-outline">
                <i class="fas fa-user-graduate"></i> Solicitudes de personal
            </a>
            <a href="${pageContext.request.contextPath}/instalaciones/" class="btn btn-outline">
                <i class="fas fa-eye"></i> Vista Pública
            </a>
        </div>
    </div>
</div>