<%-- 
    Document   : solicitudesProfesor
    Created on : 26 oct 2025, 0:39:41
    Author     : agustinrodriguez
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="panel-content">
    <div class="table-container">
        <div class="table-header">
            <div class="header-title-with-button">
                <h2>Solicitudes de Profesor</h2>
                <a href="${pageContext.request.contextPath}/usuario/panel" class="btn-solicitudes-panel">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                    </svg>
                    Volver al Panel
                </a>
            </div>
            <div class="table-stats">
                <span class="stat-badge">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    ${solicitudes.size()} solicitudes
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
            <c:when test="${empty solicitudes}">
                <div class="empty-state">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    <h3>No hay solicitudes pendientes</h3>
                    <p>Todas las solicitudes han sido revisadas.</p>
                    <a href="${pageContext.request.contextPath}/usuario/panel" class="btn-empty-action">
                        Volver al panel de usuarios
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="solicitudes-container">
                    <div class="solicitudes-grid">
                        <c:forEach var="solicitud" items="${solicitudes}">
                            <div class="solicitud-card">
                                <div class="solicitud-header">
                                    <div class="solicitud-user">
                                        <div class="solicitud-avatar">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                            </svg>
                                        </div>
                                        <div class="solicitud-user-info">
                                            <h4 class="solicitud-name">${solicitud.nombre}</h4>
                                            <span class="solicitud-id">ID: ${solicitud.id}</span>
                                        </div>
                                    </div>
                                    <span class="solicitud-badge solicitud-badge-pendiente">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                        </svg>
                                        Pendiente
                                    </span>
                                </div>
                                
                                <div class="solicitud-info">
                                    <p><strong>DNI:</strong> ${solicitud.dni}</p>
                                    <p><strong>Email:</strong> ${solicitud.email}</p>
                                    <p><strong>Rol actual:</strong> 
                                        <c:choose>
                                            <c:when test="${solicitud.rol == 1}">
                                                <span class="role-tag role-tag-student">Estudiante</span>
                                            </c:when>
                                            <c:when test="${solicitud.rol == 2}">Profesor</c:when>
                                            <c:otherwise>Desconocido</c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                
                                <div class="solicitud-actions">
                                    <form action="${pageContext.request.contextPath}/usuario/aprobarSolicitud" method="post" style="display: contents;">
                                        <input type="hidden" name="usuarioId" value="${solicitud.id}">
                                        <button type="submit" class="btn-solicitud btn-solicitud-aprobar">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                                            </svg>
                                            Aprobar
                                        </button>
                                    </form>
                                    <form action="${pageContext.request.contextPath}/usuario/rechazarSolicitud" method="post" style="display: contents;">
                                        <input type="hidden" name="usuarioId" value="${solicitud.id}">
                                        <button type="submit" class="btn-solicitud btn-solicitud-rechazar">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                                            </svg>
                                            Rechazar
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>