<%-- 
    Document   : registro
    Created on : 24 oct 2025, 10:34:59
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registro - Universidad de Huelva</title>
        <link rel="stylesheet" href="../styles/usuarios.css"/>
    </head>
    <body class="auth-body">
        <div class="auth-container">
            <div class="auth-card">
                <a href="/instalaciones-uhu-master" class="auth-logo">
                    <img src="../img/logoUHU._Horizontal_Color_Positivo.svg" alt="Universidad de Huelva">
                </a>
                
                <div class="auth-header">
                    <h1>Crear cuenta</h1>
                    <p>Únete a la comunidad deportiva de la UHU</p>
                </div>

                <form class="auth-form" action="/instalaciones-uhu-master/usuario/save" method="post">
                    <div class="form-group">
                        <label for="dni">DNI</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2"></path>
                            </svg>
                            <input type="text" id="dni" name="dni" placeholder="12345678A" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="nombre">Nombre completo</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                            </svg>
                            <input type="text" id="nombre" name="nombre" placeholder="Juan Pérez García" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="email">Correo electrónico</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                            </svg>
                            <input type="email" id="email" name="email" placeholder="usuario@uhu.es" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">Contraseña</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                            </svg>
                            <input type="password" id="password" name="password" placeholder="Mínimo 8 caracteres" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="rol">Tipo de usuario</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                            </svg>
                            <select id="rol" name="rol" required>
                                <option value="" disabled selected>Selecciona tu rol</option>
                                <option value="1">Estudiante / Usuario</option>
                                <option value="2">Profesor / Personal</option>
                            </select>
                        </div>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        Crear cuenta
                    </button>
                </form>

                <div class="auth-divider">
                    <span>¿Ya tienes cuenta?</span>
                </div>

                <a href="login" class="btn-auth-secondary">
                    Iniciar sesión
                </a>

                <footer class="auth-footer">
                    <p>Universidad de Huelva - Dr. Cantero Cuadrado, 6. 21071 Huelva</p>
                    <p>Teléfono: +34 (959) 21800</p>
                </footer>
            </div>
        </div>
    </body>
</html>