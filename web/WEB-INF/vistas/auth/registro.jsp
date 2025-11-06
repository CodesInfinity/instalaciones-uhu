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
        <script src="../scripts/registro.js"></script>
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
                            <input type="password" id="password" name="password" placeholder="Mínimo 6 caracteres" required>
                        </div>
                        <small class="form-hint">La contraseña debe tener al menos 6 caracteres</small>
                    </div>

                    <%-- Campo oculto para rol de estudiante --%>
                    <input type="hidden" name="rol" value="1">

                    <div class="form-group">
                        <label>Solicitud de cuenta especial</label>
                        <div class="checkbox-group">
                            <label class="checkbox-option">
                                <input type="checkbox" name="solicitarProfesor" value="true" id="solicitarProfesor">
                                <div class="checkbox-label">
                                    <span>Solicitar cuenta de Profesor / Personal Universitario</span>
                                    <small class="form-hint">
                                        Marca esta opción si eres profesor, investigador o personal de la Universidad de Huelva
                                    </small>
                                </div>
                            </label>
                        </div>

                        <%-- Información sobre la solicitud --%>
                        <div class="solicitud-info" id="infoSolicitud" style="display: none;">
                            <h4>📋 Información sobre la solicitud</h4>
                            <p>Tu cuenta se creará como Estudiante / Usuario y tu solicitud será revisada por el administrador. 
                                Recibirás un correo cuando tu cuenta sea actualizada a Profesor.</p>
                        </div>
                    </div>

                    <button type="submit" class="btn-auth-primary">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 20px; height: 20px; margin-right: 8px;">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path>
                        </svg>
                        Crear cuenta
                    </button>
                </form>

                <div class="auth-divider">
                    <span>¿Ya tienes cuenta?</span>
                </div>

                <a href="login" class="btn-auth-secondary">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 20px; height: 20px; margin-right: 8px;">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"></path>
                    </svg>
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