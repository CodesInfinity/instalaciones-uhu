<%-- 
    Document   : login.jsp
    Created on : 24 oct 2025, 10:34:59
    Author     : agustinrodriguez
    Description: PÁGINA DE INICIO DE SESIÓN
                 Punto de entrada principal para usuarios registrados.
                 Incluye feedback de errores y validación básica.
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
        
        <script src="${pageContext.request.contextPath}/scripts/validacion-formularios.js" defer></script>
        
        <style>
            /* Spinner pequeño para el botón de submit */
            .spinner-small {
                width: 20px;
                height: 20px;
                border: 2px solid #ffffff;
                border-top: 2px solid transparent;
                border-radius: 50%;
                animation: spin 0.8s linear infinite;
                display: inline-block;
            }
            @keyframes spin { to { transform: rotate(360deg); } }
        </style>
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

                <form class="auth-form" 
                      id="formLogin"
                      action="${pageContext.request.contextPath}/usuario/login" 
                      method="post"
                      onsubmit="activarLoading()">
                    
                    <div class="form-group">
                        <label for="email">Correo electrónico</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                            </svg>
                            <input type="email" 
                                   id="email" 
                                   name="email" 
                                   placeholder="Introduce tu email" 
                                   required 
                                   value="${param.email}">
                        </div>
                        <small class="validation-message"></small>
                    </div>

                    <div class="form-group">
                        <label for="password">Contraseña</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                            </svg>
                            <input type="password" 
                                   id="password" 
                                   name="password" 
                                   placeholder="Introduce tu contraseña" 
                                   required
                                   minlength="6">
                        </div>
                        <small class="validation-message"></small>
                    </div>

                    <button type="submit" class="btn-auth-primary" id="btnSubmit">
                        <span class="btn-text">Iniciar sesión</span>
                        <span class="btn-spinner" style="display: none;">
                            <div class="spinner-small"></div>
                        </span>
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

        <script>
            function activarLoading() {
                const btn = document.getElementById('btnSubmit');
                const text = btn.querySelector('.btn-text');
                const spinner = btn.querySelector('.btn-spinner');
                
                // Deshabilitar botón y mostrar spinner
                btn.style.opacity = '0.8';
                btn.style.cursor = 'wait';
                text.style.display = 'none';
                spinner.style.display = 'inline-block';
                
                // Permitir el envío real del formulario
                return true; 
            }
        </script>
    </body>
</html>