<%-- 
    Document   : editUsuario
    Created on : 26 oct 2025, 0:39:41
    Author     : agustinrodriguez
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="panel-content">
    <div class="auth-card edit-card">
        <a href="${pageContext.request.contextPath}/usuario/panel" class="back-link">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
            </svg>
            Volver al panel
        </a>

        <div class="auth-header">
            <h1>Editar Usuario</h1>
            <p>Modifica los datos del usuario seleccionado</p>
        </div>

        <form class="auth-form" action="${pageContext.request.contextPath}/usuario/save" method="post">
            <input type="hidden" name="id" value="${usuario.id}"/>

            <div class="form-group">
                <label for="dni">DNI</label>
                <div class="input-wrapper">
                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"></path>
                    </svg>
                    <input type="text" id="dni" name="dni" value="${usuario.dni}" required>
                </div>
            </div>

            <div class="form-group">
                <label for="nombre">Nombre completo</label>
                <div class="input-wrapper">
                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                    </svg>
                    <input type="text" id="nombre" name="nombre" value="${usuario.nombre}" required>
                </div>
            </div>

            <div class="form-group">
                <label for="email">Correo electrónico</label>
                <div class="input-wrapper">
                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <input type="email" id="email" name="email" value="${usuario.email}" required>
                </div>
            </div>

            <div class="form-group">
                <label for="password">Nueva contraseña (opcional)</label>
                <div class="input-wrapper">
                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                    </svg>
                    <input type="password" id="password" name="password" placeholder="Dejar en blanco para mantener la actual">
                </div>
                <small class="form-hint">Solo completa este campo si deseas cambiar la contraseña</small>
            </div>

            <div class="form-group">
                <label for="rol">Tipo de usuario</label>
                <div class="input-wrapper">
                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                    </svg>
                    <select id="rol" name="rol" required>
                        <option value="1" ${usuario.rol == 1 ? 'selected' : ''}>Estudiante / Usuario</option>
                        <option value="2" ${usuario.rol == 2 ? 'selected' : ''}>Profesor / Personal</option>
                    </select>
                </div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn-auth-primary">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    Guardar cambios
                </button>
                <a href="${pageContext.request.contextPath}/usuario/panel" class="btn-auth-secondary">
                    Cancelar
                </a>
            </div>
        </form>
    </div>
</div>