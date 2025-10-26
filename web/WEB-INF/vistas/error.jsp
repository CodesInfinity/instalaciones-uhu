<%-- 
    Document   : error
    Created on : 24 oct 2025, 18:45:00
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Error - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/usuarios.css"/>
    </head>
    <body class="auth-body">
        <div class="auth-container">
            <div class="auth-card error-card">
                <a href="/instalaciones-uhu-master" class="auth-logo">
                    <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="Universidad de Huelva">
                </a>

                <div class="error-icon">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                    </svg>
                </div>

                <div class="auth-header">
                    <h1>Ha ocurrido un error</h1>
                    <p>No se pudo completar la operación solicitada</p>
                </div>

                <div class="error-message-box">
                    <%
                        String msg = (String) request.getAttribute("msg");
                        if (msg != null && !msg.isEmpty()) {
                    %>
                        <p><%= msg %></p>
                    <%
                        } else {
                    %>
                        <p>Se ha producido un error inesperado. Por favor, inténtalo de nuevo más tarde o contacta con el administrador del sistema.</p>
                    <%
                        }
                    %>
                </div>
                    <a href="javascript:history.back()" class="btn-auth-secondary">
                        Volver atrás
                    </a>
                </div>

                <footer class="auth-footer">
                    <p>Universidad de Huelva - Dr. Cantero Cuadrado, 6. 21071 Huelva</p>
                    <p>Teléfono: +34 (959) 21800</p>
                </footer>
            </div>
        </div>
    </body>
</html>