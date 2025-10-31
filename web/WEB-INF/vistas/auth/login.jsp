<%-- 
    Document   : registro
    Created on : 24 oct 2025, 10:34:59
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Iniciar Sesión - Universidad de Huelva</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/usuarios.css"/>
    </head>
    <body class="auth-body">
        <div class="auth-container">
            <div class="auth-card">
                <a href="${pageContext.request.contextPath}/" class="auth-logo">
                    <img src="${pageContext.request.contextPath}/img/logoUHU._Horizontal_Color_Positivo.svg" alt="Universidad de Huelva">
                </a>
                
                <div class="auth-header">
                    <h1>Bienvenido de nuevo</h1>
                    <p>Accede a tu cuenta de instalaciones deportivas</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="auth-error">
                        ${error}
                    </div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="auth-success">
                        ${success}
                    </div>
                </c:if>

                <form class="auth-form" action="${pageContext.request.contextPath}/usuario/login" method="post">
                    <div class="form-group">
                        <label for="email">Correo electrónico</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                            </svg>
                            <input type="email" id="email" name="email" placeholder="Introduce tu email" required value="${param.email}">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">Contraseña</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                            </svg>
                            <input type="password" id="password" name="password" placeholder="Introduce tu contraseña" required>
                        </div>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        Iniciar sesión
                    </button>
                </form>

                <div class="auth-divider">
                    <span>¿No tienes cuenta?</span>
                </div>

                <a href="${pageContext.request.contextPath}/usuario/registro" class="btn-auth-secondary">
                    Crear cuenta nueva
                </a>

                <footer class="auth-footer">
                    <p>Universidad de Huelva - Dr. Cantero Cuadrado, 6. 21071 Huelva</p>
                    <p>Teléfono: +34 (959) 21800</p>
                </footer>
            </div>
        </div>
    </body>
</html>