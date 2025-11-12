<%-- 
    Document   : listaInstalaciones
    Created on : 6 nov 2025, 20:13:54
    Author     : agustinrodriguez
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="instalaciones-container">
    <div class="instalaciones-header">
        <div class="header-content">
            <span class="badge">Instalaciones Deportivas</span>
            <h1 class="page-title">Nuestras Instalaciones</h1>
            <p class="page-subtitle">Descubre todos nuestros espacios deportivos disponibles</p>
        </div>
        <c:if test="${sessionScope.usuario.rol == 0}">
            <a href="${pageContext.request.contextPath}/instalaciones/nueva" class="btn btn-primary">
                <i class="fas fa-plus-circle"></i> Nueva Instalación
            </a>
        </c:if>
    </div>

    <div class="instalaciones-grid">
        <c:choose>
            <c:when test="${not empty instalaciones}">
                <c:forEach var="instalacion" items="${instalaciones}">
                    <div class="instalacion-card">
                        <c:choose>
                            <c:when test="${not empty instalacion.imagenUrl}">
                                <div class="card-header-with-image">
                                    <img src="${pageContext.request.contextPath}${instalacion.imagenUrl}" 
                                         alt="${instalacion.nombre}" 
                                         class="card-header-image"
                                         onerror="this.style.display='none'; this.parentNode.classList.add('card-header-default')">
                                    <div class="card-header-overlay">
                                        <h3 class="card-title-overlay">${instalacion.nombre}</h3>
                                        <span class="badge badge-overlay">
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
                                                <c:when test="${instalacion.tipo == 'Pádel'}">
                                                    <i class="fas fa-table-tennis"></i>
                                                </c:when>
                                                <c:when test="${instalacion.tipo == 'Natación'}">
                                                    <i class="fas fa-swimmer"></i>
                                                </c:when>
                                                <c:when test="${instalacion.tipo == 'Gimnasio'}">
                                                    <i class="fas fa-dumbbell"></i>
                                                </c:when>
                                                <c:when test="${instalacion.tipo == 'Polideportivo'}">
                                                    <i class="fas fa-warehouse"></i>
                                                </c:when>
                                                <c:when test="${instalacion.tipo == 'Atletismo'}">
                                                    <i class="fas fa-running"></i>
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
                            <c:otherwise>
                                <div class="card-header-default">
                                    <div class="default-icon">
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
                                            <c:when test="${instalacion.tipo == 'Pádel'}">
                                                <i class="fas fa-table-tennis"></i>
                                            </c:when>
                                            <c:when test="${instalacion.tipo == 'Natación'}">
                                                <i class="fas fa-swimmer"></i>
                                            </c:when>
                                            <c:when test="${instalacion.tipo == 'Gimnasio'}">
                                                <i class="fas fa-dumbbell"></i>
                                            </c:when>
                                            <c:when test="${instalacion.tipo == 'Polideportivo'}">
                                                <i class="fas fa-warehouse"></i>
                                            </c:when>
                                            <c:when test="${instalacion.tipo == 'Atletismo'}">
                                                <i class="fas fa-running"></i>
                                            </c:when>
                                            <c:otherwise>
                                                <i class="fas fa-building"></i>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="card-header-overlay">
                                        <h3 class="card-title-overlay">${instalacion.nombre}</h3>
                                        <span class="badge badge-overlay">
                                            <i class="fas fa-tag"></i> ${instalacion.tipo}
                                        </span>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="card-body">
                            <div class="info-item">
                                <i class="fas fa-map-marker-alt text-danger"></i>
                                <span>${instalacion.ubicacion}</span>
                            </div>
                            <p class="card-description">
                                <i class="fas fa-align-left text-muted"></i>
                                ${not empty instalacion.descripcion ? instalacion.descripcion : 'Sin descripción disponible.'}
                            </p>
                        </div>
                        <div class="card-footer">
                            <div class="card-actions">
                                <a href="${pageContext.request.contextPath}/instalaciones/detalle?id=${instalacion.id}" 
                                   class="btn btn-outline">
                                    <i class="fas fa-eye"></i> Ver Detalles
                                </a>
                                <c:if test="${sessionScope.usuario.rol == 0}">
                                    <div class="admin-actions">
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
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-icon">
                        <i class="fas fa-building"></i>
                    </div>
                    <h3>No hay instalaciones disponibles</h3>
                    <p>Próximamente agregaremos nuevas instalaciones deportivas.</p>
                    <c:if test="${sessionScope.usuario.rol == 0}">
                        <a href="${pageContext.request.contextPath}/instalaciones/nueva" class="btn btn-primary">
                            <i class="fas fa-plus-circle"></i> Agregar Primera Instalación
                        </a>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>