<%-- 
    Document   : registro
    Created on : 24 oct 2025, 11:07:05
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registro de Usuario - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/index.css"/>
    </head>
    <body class="body-form">
        <div class="login-container">
            <a href="/instalaciones-uhu-master">
                <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="uhu.es logo" class="logo">
            </a>
            <form class="login-form" action="/instalaciones-uhu-master/usuario/save" method="post">
                <input type="text" name="dni" placeholder="DNI" required>
                <input type="text" name="nombre" placeholder="Nombre completo" required>
                <input type="email" name="email" placeholder="Correo electrónico" required>
                <input type="password" name="password" placeholder="Contraseña" required>
                <select name="rol" required>
                    <option value="" disabled selected>Selecciona un rol</option>
                    <option value="0">Usuario</option>
                    <option value="1">Profesor</option>
                </select>
                <button type="submit">Registrarse</button>
            </form>

            <p class="help-text">
                ¿Ya tienes cuenta? <a href="login">Inicia sesión aquí</a>
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
