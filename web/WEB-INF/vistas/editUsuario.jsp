<%-- 
    Document   : editUsuario
    Created on : 26 oct 2025, 0:39:41
    Author     : agustinrodriguez
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Editar Usuario - Universidad de Huelva</title>
    <link rel="stylesheet" href="../styles/index.css"/>
</head>
<body>

<div class="user-form-container">
    <h1 class="section-title">Editar Usuario</h1>
    <p class="section-description">Modifica los datos del usuario seleccionado.</p>

    <form class="user-form" action="${pageContext.request.contextPath}/usuario/update" method="post">
        <input type="hidden" name="id" value="${usuario.id}"/>

        <label for="dni">DNI</label>
        <input type="text" id="dni" name="dni" value="${usuario.dni}" required/>

        <label for="nombre">Nombre</label>
        <input type="text" id="nombre" name="nombre" value="${usuario.nombre}" required/>

        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="${usuario.email}" required/>

        <label for="password">Contraseña</label>
        <input type="password" id="password" name="password" placeholder="Dejar en blanco para mantener"/>

        <label for="rol">Rol</label>
        <select id="rol" name="rol" required>
            <option value="1" ${usuario.rol == 1 ? "selected" : ""}>Usuario</option>
            <option value="2" ${usuario.rol == 2 ? "selected" : ""}>Profesor</option>
        </select>

        <button type="submit" class="btn-primary">Guardar cambios</button>
        <a href="${pageContext.request.contextPath}/usuario/panel" class="btn-secondary">Cancelar</a>
    </form>
</div>

</body>
</html>
