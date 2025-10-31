/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package app.controladores;

import app.modelos.Usuario;
import app.servicios.AuthService;
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
import jakarta.servlet.http.HttpSession;
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
@WebServlet(name = "controladorUsuario", urlPatterns = {"/usuario/*"})
public class controladorUsuario extends HttpServlet {

    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    @Resource
    private UserTransaction utx;
    private static final Logger Log = Logger.getLogger(controladorUsuario.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/login";

        switch (path) {
            case "/panel" -> {
                // El AdminFilter ya verificó los permisos
                List<Usuario> usuarios = obtenerUsuarios();
                request.setAttribute("usuarios", usuarios);
                forward(request, response, "/WEB-INF/vistas/admin/panelUsuarios.jsp");
            }
            case "/editar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    Usuario usuario = em.find(Usuario.class, Long.parseLong(idParam));
                    request.setAttribute("usuario", usuario);
                    forward(request, response, "/WEB-INF/vistas/admin/editarUsuario.jsp");
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
                forward(request, response, "/WEB-INF/vistas/auth/registro.jsp");
            case "/login" ->
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            case "/logout" -> {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/");
            }
            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            case "/save" -> {
                procesarGuardarUsuario(request, response);
            }
            case "/login" -> {
                procesarLogin(request, response);
            }
            default ->
                forwardError(request, response, "Acción no válida");
        }
    }

    private void procesarGuardarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        String dni = request.getParameter("dni");
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rolParam = request.getParameter("rol");

        try {
            // Validar campos requeridos - el rol es opcional en registro público
            if (dni == null || nombre == null || email == null
                    || dni.trim().isEmpty() || nombre.trim().isEmpty() || email.trim().isEmpty()) {

                throw new Exception("Todos los campos obligatorios deben ser completados");
            }

            // Validar formato de email
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new Exception("El formato del email no es válido");
            }

            int rol;
            // Determinar el rol - si es registro público, usar rol por defecto (1 = Estudiante)
            if (rolParam == null || rolParam.trim().isEmpty()) {
                rol = 1; // Rol por defecto para registro público
            } else {
                rol = Integer.parseInt(rolParam);
            }

            Usuario usuario;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // MODO EDICIÓN - Actualizar usuario existente
                Long id = Long.parseLong(idParam);
                usuario = em.find(Usuario.class, id);

                if (usuario == null) {
                    throw new Exception("Usuario no encontrado para editar");
                }

                // Verificar si el email o DNI ya existen en otros usuarios (excepto el actual)
                Usuario usuarioExistente = findByEmailOrDniExcludingId(email, dni, id);
                if (usuarioExistente != null) {
                    if (usuarioExistente.getEmail().equals(email)) {
                        throw new Exception("Ya existe un usuario con ese email");
                    }
                    if (usuarioExistente.getDni().equals(dni)) {
                        throw new Exception("Ya existe un usuario con ese DNI");
                    }
                }

                // Actualizar datos del usuario
                usuario.setDni(dni.trim());
                usuario.setNombre(nombre.trim());
                usuario.setEmail(email.trim().toLowerCase());
                usuario.setRol(rol);

                // Solo actualizar password si se proporcionó uno nuevo
                if (password != null && !password.trim().isEmpty()) {
                    if (password.length() < 6) {
                        throw new Exception("La contraseña debe tener al menos 6 caracteres");
                    }
                    String hashedPassword = hashPassword(password);
                    usuario.setPassword(hashedPassword);
                }

            } else {
                // MODO REGISTRO - Crear nuevo usuario
                if (password == null || password.trim().isEmpty()) {
                    throw new Exception("La contraseña es requerida para el registro");
                }

                if (password.length() < 6) {
                    throw new Exception("La contraseña debe tener al menos 6 caracteres");
                }

                // Verificar si el usuario ya existe
                Usuario usuarioExistente = findByEmailOrDni(email, dni);
                if (usuarioExistente != null) {
                    if (usuarioExistente.getEmail().equalsIgnoreCase(email)) {
                        throw new Exception("Ya existe un usuario registrado con ese email");
                    }
                    if (usuarioExistente.getDni().equals(dni)) {
                        throw new Exception("Ya existe un usuario registrado con ese DNI");
                    }
                }

                // Crear nuevo usuario
                String hashedPassword = hashPassword(password);
                usuario = new Usuario(
                        dni.trim(),
                        nombre.trim(),
                        email.trim().toLowerCase(),
                        hashedPassword,
                        rol // Usar el rol determinado arriba
                );
            }

            // Guardar usuario en la base de datos
            save(usuario);

            // Redirigir según el contexto
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("usuario") != null) {
                // Si hay usuario logueado, redirigir al panel de admin
                response.sendRedirect(request.getContextPath() + "/usuario/panel");
            } else {
                // Si no hay usuario logueado (registro nuevo), redirigir al login
                request.setAttribute("success", "Usuario registrado correctamente. Puede iniciar sesión.");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }

        } catch (NumberFormatException e) {
            forwardError(request, response, "El formato del rol no es válido");
        } catch (Exception e) {
            // Mantener los datos en el request para mostrarlos en el formulario
            request.setAttribute("dni", dni);
            request.setAttribute("nombre", nombre);
            request.setAttribute("email", email);
            request.setAttribute("rol", rolParam);

            if (idParam != null && !idParam.trim().isEmpty()) {
                request.setAttribute("error", "Error al actualizar usuario: " + e.getMessage());
                forward(request, response, "/WEB-INF/vistas/admin/editarUsuario.jsp");
            } else {
                request.setAttribute("error", "Error al registrar usuario: " + e.getMessage());
                forward(request, response, "/WEB-INF/vistas/auth/registro.jsp");
            }
        }
    }

    private Usuario findByEmailOrDniExcludingId(String email, String dni, Long excludeId) {
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE (u.email = :email OR u.dni = :dni) AND u.id != :excludeId",
                    Usuario.class);
            query.setParameter("email", email);
            query.setParameter("dni", dni);
            query.setParameter("excludeId", excludeId);
            List<Usuario> resultados = query.getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error al buscar usuario existente excluyendo ID", e);
            return null;
        }
    }

    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Log.log(Level.INFO, "Intento de login con email: {0}", email);

            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("error", "Email y contraseña son requeridos");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
                return;
            }

            AuthService authService = new AuthService();
            authService.em = em;
            Usuario usuario = authService.autenticarPorEmail(email, password);

            if (usuario != null) {
                Log.log(Level.INFO, "Login exitoso para usuario: {0}, rol: {1}",
                        new Object[]{usuario.getNombre(), usuario.getRol()});

                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);

                // Redirigir según el rol
                if (usuario.getRol() == 0) {
                    Log.log(Level.INFO, "Redirigiendo admin a panel");
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    Log.log(Level.INFO, "Redirigiendo usuario regular a inicio");
                    response.sendRedirect(request.getContextPath() + "/");
                }
            } else {
                Log.log(Level.WARNING, "Login fallido para email: {0}", email);
                request.setAttribute("error", "Email o contraseña incorrectos");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }
        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error en el login", e);
            forwardError(request, response, "Error en el login: " + e.getMessage());
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
