<%-- 
    Document   : panelUsuarios
    Created on : 26 oct 2025, 0:30:04
    Author     : agustinrodriguez
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Panel de Usuarios - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/index.css"/>
    </head>
    <body>

        <div class="admin-container">
            <a href="/instalaciones-uhu-master">
                <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="uhu.es logo" class="logo">
            </a>
            <hr>
            <br>
            <h1 class="section-title">Panel de Usuarios</h1>
            <p class="section-description">Gestiona todos los usuarios registrados en el sistema.</p>

            <div class="table-wrapper">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>DNI</th>
                            <th>Nombre</th>
                            <th>Email</th>
                            <th>Rol</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="usuario" items="${usuarios}">
                            <tr>
                                <td data-label="DNI">${usuario.dni}</td>
                                <td data-label="Nombre">${usuario.nombre}</td>
                                <td data-label="Email">${usuario.email}</td>
                                <td data-label="Rol">
                                    <c:choose>
                                        <c:when test="${usuario.rol == 1}">Usuario</c:when>
                                        <c:when test="${usuario.rol == 2}">Profesor</c:when>
                                    </c:choose>
                                </td>
                                <td data-label="Acciones">
                                    <div class="admin-actions">
                                        <!-- Editar -->
                                        <a href="${pageContext.request.contextPath}/usuario/edit?id=${usuario.id}" title="Editar">
                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2">
                                            <path stroke-linecap="round" stroke-linejoin="round"
                                                  d="M16.862 3.487a2.25 2.25 0 013.182 3.182l-10.5 10.5a2.25 2.25 0 01-1.004.591l-4 1a0.75.75 0 01-.922-.922l1-4a2.25 2.25 0 01.591-1.004l10.5-10.5z"/>
                                            </svg>
                                        </a>
                                        <!-- Borrar -->
                                        <a href="${pageContext.request.contextPath}/usuario/delete?id=${usuario.id}" title="Borrar"
                                           onclick="return confirm('¿Seguro que quieres eliminar este usuario?');">
                                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2">
                                            <path stroke-linecap="round" stroke-linejoin="round"
                                                  d="M6 18L18 6M6 6l12 12"/>
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

    </body>
</html>
