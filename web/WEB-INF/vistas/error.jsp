<%-- 
    Document   : error
    Created on : 24 oct 2025, 18:45:00
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Error - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/index.css"/>
    </head>
    <body class="body-form">
        <div class="login-container">
            <a href="/instalaciones-uhu-master">
                <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="uhu.es logo" class="logo">
            </a>

            <h2 class="error-title">Ha ocurrido un error</h2>

            <div class="error-message">
                <%
                    String msg = (String) request.getAttribute("msg");
                    if (msg != null && !msg.isEmpty()) {
                %>
                    <p><%= msg %></p>
                <%
                    } else {
                %>
                    <p>Se ha producido un error inesperado. Por favor, inténtalo de nuevo más tarde.</p>
                <%
                    }
                %>
            </div>

            <a href="javascript:history.back()" class="error-button">Volver atrás</a>

            <footer class="form-usuarios">
                <p>
                    English | Español | Administración del consentimiento<br>
                    Universidad de Huelva, Todos los Derechos Reservados - Dr. Cantero Cuadrado, 6. 21071 Huelva<br>
                    Teléfono: +34 (959) 21800
                </p>
            </footer>
        </div>
    </body>
</html>
