<%-- 
    Document   : layout
    Created on : 4 nov 2025, 20:18:10
    Author     : agustinrodriguez
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><c:out value="${pageTitle}" default="Universidad de Huelva"/></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/usuarios.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/espacios.css"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/reservas.css"/>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    </head>
    <body class="panel-body">
        <!-- NAVBAR PROFESIONAL -->
        <nav class="professional-nav">
            <div class="nav-container">
                <div class="nav-content">
                    <!-- Logo -->
                    <div class="nav-brand">
                        <a href="${pageContext.request.contextPath}/" class="brand-link">
                            <img src="${pageContext.request.contextPath}/img/logoUHU._Horizontal_Color_Positivo.svg" 
                                 alt="Universidad de Huelva" class="brand-logo">
                            <span class="brand-subtitle">Instalaciones Deportivas</span>
                        </a>
                    </div>

                    <!-- Menú Desktop -->
                    <div class="nav-menu-desktop">
                        <div class="nav-links">
                            <a href="${pageContext.request.contextPath}/#servicios" class="nav-link">Servicios</a>
                            <!-- AGREGAR ENLACE A INSTALACIONES -->
                            <a href="${pageContext.request.contextPath}/instalaciones/" class="nav-link">Instalaciones</a>
                            <a href="${pageContext.request.contextPath}/reservas/" class="nav-link">Reservas</a>
                        </div>

                        <div class="nav-actions">
                            <c:choose>
                                <c:when test="${not empty sessionScope.usuario}">
                                    <div class="user-profile">
                                        <div class="user-avatar-nav">
                                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                            </svg>
                                        </div>
                                        <div class="user-info-nav">
                                            <span class="user-name-nav">${sessionScope.usuario.nombre}</span>
                                            <span class="user-role-nav ${sessionScope.usuario.rol == 0 ? 'role-admin' : 
                                                                         sessionScope.usuario.rol == 1 ? 'role-user' : 
                                                                         'role-profesor'}">
                                                  <c:choose>
                                                      <c:when test="${sessionScope.usuario.rol == 0}">Administrador</c:when>
                                                      <c:when test="${sessionScope.usuario.rol == 1}">Estudiante</c:when>
                                                      <c:when test="${sessionScope.usuario.rol == 2}">Profesor</c:when>
                                                  </c:choose>
                                            </span>
                                        </div>
                                        <div class="user-dropdown">
                                            <!-- AGREGAR OPCIONES DE ADMINISTRACIÓN PARA INSTALACIONES -->
                                            <c:if test="${sessionScope.usuario.rol == 0}">
                                                <a href="${pageContext.request.contextPath}/usuario/panel" class="dropdown-item">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                                    </svg>
                                                    Gestión de Usuarios
                                                </a>
                                                <a href="${pageContext.request.contextPath}/usuario/solicitudes" class="dropdown-item">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                                    </svg>
                                                    Solicitudes de Profesor
                                                </a>
                                                <a href="${pageContext.request.contextPath}/instalaciones/panel" class="dropdown-item">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                          d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                                                    </svg>
                                                    Gestión de Instalaciones
                                                </a>
                                                <a href="${pageContext.request.contextPath}/reservas/panel" class="dropdown-item">
                                                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                                    </svg>
                                                    Gestión de Reservas
                                                </a>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/usuario/logout" class="dropdown-item logout-item">
                                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                                                </svg>
                                                Cerrar Sesión
                                            </a>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="auth-buttons">
                                        <a href="${pageContext.request.contextPath}/usuario/login" class="btn-nav btn-nav-login">Iniciar Sesión</a>
                                        <a href="${pageContext.request.contextPath}/usuario/registro" class="btn-nav btn-nav-primary">Registrarse</a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Hamburger Button -->
                    <button class="nav-toggle" id="navToggle" aria-label="Menú de navegación">
                        <span class="hamburger-line"></span>
                        <span class="hamburger-line"></span>
                        <span class="hamburger-line"></span>
                    </button>
                </div>
            </div>

            <!-- Menú Mobile -->
            <div class="nav-menu-mobile" id="navMenuMobile">
                <div class="mobile-nav-content">
                    <div class="mobile-nav-links">
                        <a href="${pageContext.request.contextPath}/#servicios" class="mobile-nav-link">Servicios</a>
                        <!-- AGREGAR ENLACE A INSTALACIONES EN MÓVIL -->
                        <a href="${pageContext.request.contextPath}/instalaciones/" class="mobile-nav-link">Instalaciones</a>
                        <a href="${pageContext.request.contextPath}/reservas/" class="mobile-nav-link">Reservas</a>
                    </div>

                    <c:choose>
                        <c:when test="${not empty sessionScope.usuario}">
                            <div class="mobile-user-section">
                                <div class="mobile-user-info">
                                    <div class="user-avatar-nav mobile">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                                        </svg>
                                    </div>
                                    <div class="mobile-user-details">
                                        <span class="user-name-nav">${sessionScope.usuario.nombre}</span>
                                        <span class="user-role-nav ${sessionScope.usuario.rol == 0 ? 'role-admin' : 
                                                                     sessionScope.usuario.rol == 1 ? 'role-user' : 
                                                                     'role-profesor'}">
                                              <c:choose>
                                                  <c:when test="${sessionScope.usuario.rol == 0}">Administrador</c:when>
                                                  <c:when test="${sessionScope.usuario.rol == 1}">Estudiante</c:when>
                                                  <c:when test="${sessionScope.usuario.rol == 2}">Profesor</c:when>
                                              </c:choose>
                                        </span>
                                    </div>
                                </div>
                                <div class="mobile-nav-actions">
                                    <!-- AGREGAR OPCIONES DE ADMINISTRACIÓN EN MÓVIL -->
                                    <c:if test="${sessionScope.usuario.rol == 0}">
                                        <a href="${pageContext.request.contextPath}/usuario/panel" class="btn-nav btn-nav-primary mobile">
                                            Gestión de Usuarios
                                        </a>
                                        <a href="${pageContext.request.contextPath}/instalaciones/panel" class="btn-nav btn-nav-primary mobile">
                                            Gestión de Instalaciones
                                        </a>
                                        <a href="${pageContext.request.contextPath}/usuario/solicitudes" class="btn-nav btn-nav-primary mobile">
                                            Solicitudes Profesor
                                        </a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/usuario/logout" class="btn-nav btn-nav-secondary mobile">
                                        Cerrar Sesión
                                    </a>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="mobile-auth-section">
                                <a href="${pageContext.request.contextPath}/usuario/login" class="btn-nav btn-nav-login mobile">Iniciar Sesión</a>
                                <a href="${pageContext.request.contextPath}/usuario/registro" class="btn-nav btn-nav-primary mobile">Crear Cuenta</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </nav>

        <div class="panel-container">
            <!-- CONTENIDO DINÁMICO -->
            <c:if test="${not empty pageContent}">
                <jsp:include page="${pageContent}" />
            </c:if>

            <!-- FOOTER -->
            <div class="panel-footer">
                <p>Universidad de Huelva - Sistema de Instalaciones Deportivas</p>
            </div>
        </div>

        <!-- INCLUIR EL SCRIPT DEL NAVBAR -->
        <script src="${pageContext.request.contextPath}/scripts/index.js"></script>
    </body>
</html>