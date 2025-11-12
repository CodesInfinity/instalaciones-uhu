<%-- 
    Document   : panelUsuarios
    Created on : 26 oct 2025, 0:30:04
    Author     : agustinrodriguez
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="panel-content">
    <div class="table-container">
        <div class="table-header">
            <div class="header-title-with-button">
                <h2>Usuarios registrados</h2>
                <a href="${pageContext.request.contextPath}/usuario/solicitudes" class="btn-solicitudes-panel">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    Solicitudes de personal
                </a>
            </div>
            <div class="table-stats">
                <span class="stat-badge">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                    </svg>
                    ${usuarios.size()} usuarios
                </span>
            </div>
        </div>

        <div class="table-responsive">
            <table class="modern-table">
                <thead>
                    <tr>
                        <th>Usuario</th>
                        <th>DNI</th>
                        <th>Email</th>
                        <th>Tipo</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="usuario" items="${usuarios}">
                        <tr>
                            <td>
                                <div class="table-user">
                                    <div class="user-avatar">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                        </svg>
                                    </div>
                                    <span class="user-name">${usuario.nombre}</span>
                                </div>
                            </td>
                            <td class="table-dni">${usuario.dni}</td>
                            <td class="table-email">${usuario.email}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${usuario.rol == 0}">
                                        <span class="role-badge role-admin">Administrador</span>
                                    </c:when>
                                    <c:when test="${usuario.rol == 1}">
                                        <span class="role-badge role-user">Estudiante</span>
                                    </c:when>
                                    <c:when test="${usuario.rol == 2}">
                                        <span class="role-badge role-profesor">Profesor</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="role-badge role-unknown">Desconocido</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <c:if test="${usuario.rol != 0}">
                                        <a href="${pageContext.request.contextPath}/usuario/editar?id=${usuario.id}" class="action-btn action-edit" title="Editar usuario">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                                            </svg>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/usuario/borrar?id=${usuario.id}" class="action-btn action-delete" title="Eliminar usuario" onclick="return confirm('¿Estás seguro de que deseas eliminar este usuario?')">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                            </svg>
                                        </a>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>  