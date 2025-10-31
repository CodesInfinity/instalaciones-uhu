/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.controladores;

import app.modelos.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author agustinrodriguez
 */
@WebFilter("/usuario/panel")
public class AdminFilter implements Filter {
    private static final Logger Log = Logger.getLogger(AdminFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        Log.log(Level.INFO, "AdminFilter: Verificando acceso a panel");
        
        if (session != null) {
            Object usuarioObj = session.getAttribute("usuario");
            if (usuarioObj instanceof Usuario) {
                Usuario usuario = (Usuario) usuarioObj;
                Log.log(Level.INFO, "Usuario en sesión: {0}, rol: {1}", 
                       new Object[]{usuario.getNombre(), usuario.getRol()});
                
                if (usuario.getRol() == 0) {
                    Log.log(Level.INFO, "Acceso permitido - usuario es admin");
                    chain.doFilter(request, response);
                    return;
                } else {
                    Log.log(Level.WARNING, "Acceso denegado - usuario no es admin");
                    httpRequest.setAttribute("msg", "No tiene permisos de administrador para acceder a esta página");
                    RequestDispatcher rd = httpRequest.getRequestDispatcher("/WEB-INF/vistas/error.jsp");
                    rd.forward(request, response);
                    return;
                }
            }
        }
        
        Log.log(Level.WARNING, "No hay usuario en sesión");
        httpRequest.setAttribute("msg", "Debe iniciar sesión para acceder a esta página");
        RequestDispatcher rd = httpRequest.getRequestDispatcher("/WEB-INF/vistas/error.jsp");
        rd.forward(request, response);
    }
}