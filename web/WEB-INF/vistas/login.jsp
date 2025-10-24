<%-- 
    Document   : login
    Created on : 24 oct 2025, 10:34:59
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Mi Cuenta - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/index.css"/>
    </head>
    <body class="body-form">
        <div class="login-container">
            <a href="/instalaciones-uhu-master">
                <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="uhu.es logo" class="logo">
            </a>
            <form class="login-form">
                <input type="text" placeholder="Usuario" required>
                <input type="password" placeholder="Contraseña" required>
                <button type="submit">Iniciar sesión</button>
            </form>

            <p class="help-text">
                Si todavía no tienes una cuenta, puedes crearla
                <a href="registro">aqui</a>
            </p>

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
