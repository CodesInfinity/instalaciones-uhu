/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package app.controladores;

import app.modelos.Usuario;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import jakarta.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author agustinrodriguez
 */
@WebServlet(name = "controladorUsuario", urlPatterns = {"/usuarios", "/usuario/*"})
public class controladorUsuario extends HttpServlet {

    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    @Resource
    private UserTransaction utx;
    private static final Logger Log = Logger.getLogger(controladorUsuario.class.getName());

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/login";

        switch (path) {
            case "/panel" -> {
                List<Usuario> usuarios = obtenerUsuarios();
                request.setAttribute("usuarios", usuarios);
                forward(request, response, "/WEB-INF/vistas/panelUsuarios.jsp");
            }
            case "/editar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    Usuario usuario = em.find(Usuario.class, Long.parseLong(idParam));
                    request.setAttribute("usuario", usuario);
                    forward(request, response, "/WEB-INF/vistas/editarUsuario.jsp");
                } else {
                    forwardError(request, response, "ID de usuario no proporcionado.");
                }
            }
            case "/borrar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    borrarUsuario(Long.parseLong(idParam));
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    forwardError(request, response, "ID de usuario no proporcionado.");
                }
            }
            case "/registro" ->
                forward(request, response, "/WEB-INF/vistas/registro.jsp");
            case "/login" ->
                forward(request, response, "/WEB-INF/vistas/login.jsp");
            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        if ("/save".equals(accion)) {

            String idParam = request.getParameter("id");
            String dni = request.getParameter("dni");
            String nombre = request.getParameter("nombre");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String rolParam = request.getParameter("rol");

            String hash = "35454B055CC325EA1AF2126E27707052";

            try {
                // Validar campos requeridos (excepto password en edición)
                if (dni == null || nombre == null || email == null || rolParam == null
                        || dni.isEmpty() || nombre.isEmpty() || email.isEmpty() || rolParam.isEmpty()) {
                    throw new Exception("Campos requeridos vacíos");
                }

                int rol = Integer.parseInt(rolParam);
                Usuario usuario;

                if (idParam != null && !idParam.isEmpty()) {
                    // EDICIÓN - Password es opcional
                    Long id = Long.parseLong(idParam);
                    usuario = em.find(Usuario.class, id);
                    if (usuario == null) {
                        throw new Exception("Usuario no encontrado");
                    }

                    usuario.setDni(dni);
                    usuario.setNombre(nombre);
                    usuario.setEmail(email);
                    usuario.setRol(rol);

                    // Solo actualizar password si se proporcionó uno nuevo
                    if (password != null && !password.isEmpty()) {
                        password = hashPassword(password);
                        usuario.setPassword(password);
                    }

                } else {
                    // REGISTRO - Password es requerido
                    if (password == null || password.isEmpty()) {
                        throw new Exception("La contraseña es requerida para registro");
                    }

                    Usuario existente = findByEmailOrDni(email, dni);
                    if (existente != null) {
                        request.setAttribute("msg", "El usuario ya está registrado con ese email o DNI");
                        forwardError(request, response, "Usuario existente");
                        return;
                    }

                    password = hashPassword(password);
                    usuario = new Usuario(dni, nombre, email, password, rol);
                }

                save(usuario);
                response.sendRedirect(request.getContextPath() + "/usuario/panel");

            } catch (Exception e) {
                forwardError(request, response, "Error al guardar usuario: " + e.getMessage());
            }

        } else {
            forwardError(request, response, "Acción no válida");
        }
    }

    private List<Usuario> obtenerUsuarios() {
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        return query.getResultList();
    }

    public void save(Usuario usuario) {
        Long id = usuario.getId();
        try {
            utx.begin();

            if (id == null) {
                em.persist(usuario);
                Log.log(Level.INFO, "Nuevo usuario guardado");
            } else {
                em.merge(usuario);
                Log.log(Level.INFO, "Usuario {0} actualizado", id);
            }

            utx.commit();

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Excepción al guardar usuario", e);
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                Log.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
            }
            throw new RuntimeException(e);
        }
    }

    private void borrarUsuario(Long id) {
        try {
            utx.begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null) {
                em.remove(usuario);
            }
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception ex) {
                Log.log(Level.SEVERE, null, ex);
            }
            throw new RuntimeException(e);
        }
    }

    private Usuario findByEmailOrDni(String email, String dni) {
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email OR u.dni = :dni", Usuario.class);
            query.setParameter("email", email);
            query.setParameter("dni", dni);
            List<Usuario> resultados = query.getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error al buscar usuario existente", e);
            return null;
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(vista);
        rd.forward(request, response);
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return myHash;
    }

    }
