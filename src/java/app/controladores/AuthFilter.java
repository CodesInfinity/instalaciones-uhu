/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.controladores;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author agustinrodriguez
 */
@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final Logger Log = Logger.getLogger(AuthFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Rutas públicas que no requieren autenticación
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Verificar si el usuario está autenticado para rutas protegidas
        if (session == null || session.getAttribute("usuario") == null) {
            httpRequest.setAttribute("msg", "Debe iniciar sesión para acceder a esta página");
            RequestDispatcher rd = httpRequest.getRequestDispatcher("/WEB-INF/vistas/error.jsp");
            rd.forward(request, response);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return path.equals("/") || 
               path.startsWith("/img/") || 
               path.startsWith("/styles/") || 
               path.startsWith("/scripts/") ||
               path.equals("/usuario/login") ||
               path.equals("/usuario/registro") ||
               path.equals("/error") ||
               path.equals("/usuario/save");
    }
}