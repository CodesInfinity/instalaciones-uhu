<%-- 
    Document   : panelReservas
    Created on : 13 nov 2025
    Author     : agustinrodriguez
    
    Panel administrativo de reservas - Solo para administradores
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="panel-content">
    <div class="table-container">
        <div class="table-header">
            <h2>Gestión de Reservas</h2>
            <div class="table-stats">
                <span class="stat-badge">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    ${reservas.size()} reservas
                </span>
            </div>
        </div>

        <div class="table-responsive">
            <table class="modern-table">
                <thead>
                    <tr>
                        <th>Instalación</th>
                        <th>Usuario</th>
                        <th>Fecha</th>
                        <th>Horario</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="reserva" items="${reservas}">
                        <tr>
                            <td>
                                <div class="table-user">
                                    <div class="user-avatar">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                                        </svg>
                                    </div>
                                    <div>
                                        <span class="user-name">${reserva.espacio.nombre}</span>
                                        <span class="table-email">${reserva.espacio.tipo}</span>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <div class="table-user">
                                    <div class="user-avatar">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                        </svg>
                                    </div>
                                    <span class="user-name">${reserva.usuario.nombre}</span>
                                </div>
                            </td>
                            <td class="table-dni">
                                <fmt:formatDate value="${reserva.inicioDate}" pattern="dd/MM/yyyy"/>
                            </td>
                            <td class="table-email">
                                <fmt:formatDate value="${reserva.inicioDate}" pattern="HH:mm"/> - 
                                <fmt:formatDate value="${reserva.finDate}" pattern="HH:mm"/>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <a href="${pageContext.request.contextPath}/reservas/editar?id=${reserva.id}" 
                                       class="action-btn action-edit" title="Editar reserva">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                                        </svg>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/reservas/borrar?id=${reserva.id}" 
                                       class="action-btn action-delete" title="Eliminar reserva" 
                                       onclick="return confirm('¿Estás seguro de que deseas eliminar esta reserva?')">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                                        </svg>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>