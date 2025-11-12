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
                setLayoutAttributes(request, "Panel de Usuarios",
                        "Gestiona todos los usuarios registrados en el sistema de instalaciones deportivas");
                request.setAttribute("pageContent", "../admin/panelUsuarios.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            }
            case "/solicitudes" -> {
                mostrarSolicitudesPersonal(request, response);
            }
            case "/editar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    // VERIFICACIÓN DE PERMISOS PARA EDITAR
                    if (!tienePermisoParaEditar(request, Long.parseLong(idParam))) {
                        forwardError(request, response, "No tiene permisos para editar este usuario.");
                        return;
                    }

                    Usuario usuario = em.find(Usuario.class, Long.parseLong(idParam));
                    if (usuario == null) {
                        forwardError(request, response, "Usuario no encontrado.");
                        return;
                    }

                    request.setAttribute("usuario", usuario);
                    setLayoutAttributes(request, "Editar Usuario",
                            "Modifica los datos del usuario seleccionado");
                    request.setAttribute("pageContent", "../admin/editarUsuario.jsp");
                    forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
                } else {
                    forwardError(request, response, "ID de usuario no proporcionado.");
                }
            }
            case "/borrar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    // Solo administradores pueden borrar usuarios
                    HttpSession session = request.getSession(false);
                    if (session != null && session.getAttribute("usuario") instanceof Usuario) {
                        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
                        if (usuarioLogueado.getRol() != 0) {
                            forwardError(request, response, "No tiene permisos para eliminar usuarios.");
                            return;
                        }
                    }

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
            case "/aprobarSolicitud" -> {
                procesarAprobarSolicitud(request, response);
            }
            case "/rechazarSolicitud" -> {
                procesarRechazarSolicitud(request, response);
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
        String solicitarProfesor = request.getParameter("solicitarProfesor");

        try {
            // Validar campos requeridos
            if (dni == null || nombre == null || email == null
                    || dni.trim().isEmpty() || nombre.trim().isEmpty() || email.trim().isEmpty()) {
                forwardError(request, response, "Todos los campos obligatorios deben ser completados");
                return;
            }

            // Normalizar datos
            String emailNormalizado = email.trim().toLowerCase();
            String dniNormalizado = dni.trim();
            String nombreNormalizado = nombre.trim();

            // Validar formato de email
            if (!emailNormalizado.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                forwardError(request, response, "El formato del email no es válido");
                return;
            }

            int rol;
            // Determinar el rol
            if (rolParam == null || rolParam.trim().isEmpty()) {
                rol = 1; // Rol por defecto para registro público
            } else {
                try {
                    rol = Integer.parseInt(rolParam);
                } catch (NumberFormatException e) {
                    forwardError(request, response, "El formato del rol no es válido");
                    return;
                }
            }

            // VERIFICACIÓN DE PERMISOS PARA GUARDAR (en modo edición)
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                if (!tienePermisoParaEditar(request, id)) {
                    forwardError(request, response, "No tiene permisos para editar este usuario");
                    return;
                }
            }

            Usuario usuario;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // MODO EDICIÓN - Actualizar usuario existente
                Long id = Long.parseLong(idParam);
                usuario = em.find(Usuario.class, id);

                if (usuario == null) {
                    forwardError(request, response, "Usuario no encontrado para editar");
                    return;
                }

                // Verificar si el email o DNI ya existen en otros usuarios (excepto el actual)
                Usuario usuarioExistente = findByEmailOrDniExcludingId(emailNormalizado, dniNormalizado, id);
                if (usuarioExistente != null) {
                    if (usuarioExistente.getEmail().equalsIgnoreCase(emailNormalizado)) {
                        forwardError(request, response, "Ya existe un usuario con ese email");
                        return;
                    }
                    if (usuarioExistente.getDni().equals(dniNormalizado)) {
                        forwardError(request, response, "Ya existe un usuario con ese DNI");
                        return;
                    }
                }

                // Actualizar datos del usuario
                usuario.setDni(dniNormalizado);
                usuario.setNombre(nombreNormalizado);
                usuario.setEmail(emailNormalizado);
                usuario.setRol(rol);

                // Solo actualizar password si se proporcionó uno nuevo
                if (password != null && !password.trim().isEmpty()) {
                    if (password.length() < 6) {
                        forwardError(request, response, "La contraseña debe tener al menos 6 caracteres");
                        return;
                    }
                    String hashedPassword = hashPassword(password);
                    usuario.setPassword(hashedPassword);
                }

            } else {
                // MODO REGISTRO - Crear nuevo usuario
                if (password == null || password.trim().isEmpty()) {
                    forwardError(request, response, "La contraseña es requerida para el registro");
                    return;
                }

                if (password.length() < 6) {
                    forwardError(request, response, "La contraseña debe tener al menos 6 caracteres");
                    return;
                }

                // Verificar si el usuario ya existe
                Usuario usuarioExistente = findByEmailOrDni(emailNormalizado, dniNormalizado);
                if (usuarioExistente != null) {
                    if (usuarioExistente.getEmail().equalsIgnoreCase(emailNormalizado)) {
                        forwardError(request, response, "Ya existe un usuario registrado con ese email");
                        return;
                    }
                    if (usuarioExistente.getDni().equals(dniNormalizado)) {
                        forwardError(request, response, "Ya existe un usuario registrado con ese DNI");
                        return;
                    }
                }

                // Crear nuevo usuario
                String hashedPassword = hashPassword(password);
                usuario = new Usuario(
                        dniNormalizado,
                        nombreNormalizado,
                        emailNormalizado,
                        hashedPassword,
                        rol
                );

                // PROCESAR SOLICITUD DE PROFESOR (solo para registros nuevos)
                if ("true".equals(solicitarProfesor)) {
                    usuario.setSolicitudProfesor("PENDIENTE");
                    request.setAttribute("info",
                            "Tu cuenta de estudiante ha sido creada. "
                            + "Hemos recibido tu solicitud para cuenta de profesor. "
                            + "Te contactaremos una vez sea revisada por administración."
                    );
                }
            }

            // Guardar usuario en la base de datos
            save(usuario);

            // CORRECCIÓN: Redirigir según el contexto y el rol del usuario
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("usuario") != null) {
                Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

                // Actualizar los datos del usuario en la sesión si está editando su propio perfil
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Long idEditado = Long.parseLong(idParam);
                    if (usuarioLogueado.getId().equals(idEditado)) {
                        // Actualizar la sesión con los nuevos datos
                        session.setAttribute("usuario", usuario);
                    }
                }

                // Redirigir según el rol
                if (usuarioLogueado.getRol() == 0) {
                    // Admin: redirigir al panel
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    // Usuario regular: redirigir a la página principal
                    response.sendRedirect(request.getContextPath() + "/");
                }
            } else {
                // Usuario no logueado (registro nuevo): redirigir al login
                request.setAttribute("success", "Usuario registrado correctamente. Puede iniciar sesión.");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }

        } catch (Exception e) {
            // Para cualquier otra excepción no controlada
            forwardError(request, response, "Error inesperado: " + e.getMessage());
        }
    }

    // MÉTODOS PARA GESTIÓN DE SOLICITUDES DE PROFESOR
    private void mostrarSolicitudesPersonal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdministrador(request)) {
            forwardError(request, response, "No tiene permisos para acceder a esta sección");
            return;
        }

        // Obtener usuarios con solicitud pendiente
        List<Usuario> solicitudesPendientes = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.solicitudProfesor = 'PENDIENTE'", Usuario.class)
                .getResultList();

        request.setAttribute("solicitudes", solicitudesPendientes);
        setLayoutAttributes(request, "Solicitudes de personal",
                "Gestiona las solicitudes de usuarios que quieren ser personal de la UHU");
        request.setAttribute("pageContent", "../admin/solicitudesPersonal.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void procesarAprobarSolicitud(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdministrador(request)) {
            forwardError(request, response, "No tiene permisos");
            return;
        }

        String usuarioIdParam = request.getParameter("usuarioId");
        try {
            Long usuarioId = Long.parseLong(usuarioIdParam);

            try {
                utx.begin();
                Usuario usuario = em.find(Usuario.class, usuarioId);

                if (usuario != null && "PENDIENTE".equals(usuario.getSolicitudProfesor())) {
                    // Cambiar rol a profesor y actualizar estado de solicitud
                    usuario.setRol(2); // 2 = profesor
                    usuario.setSolicitudProfesor("APROBADA");
                    em.merge(usuario);

                    utx.commit();
                    response.sendRedirect(request.getContextPath() + "/usuario/solicitudes?success=Usuario aprobado como profesor");
                } else {
                    utx.rollback();
                    forwardError(request, response, "Solicitud no encontrada o ya procesada");
                }

            } catch (Exception e) {
                try {
                    utx.rollback();
                } catch (Exception ex) {
                    Log.log(Level.SEVERE, "Error al hacer rollback", ex);
                }
                forwardError(request, response, "Error al procesar la solicitud: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            forwardError(request, response, "ID de usuario inválido");
        }
    }

    private void procesarRechazarSolicitud(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdministrador(request)) {
            forwardError(request, response, "No tiene permisos");
            return;
        }

        String usuarioIdParam = request.getParameter("usuarioId");
        try {
            Long usuarioId = Long.parseLong(usuarioIdParam);

            try {
                utx.begin();
                Usuario usuario = em.find(Usuario.class, usuarioId);

                if (usuario != null && "PENDIENTE".equals(usuario.getSolicitudProfesor())) {
                    // Solo marcar como rechazada, mantener rol de estudiante
                    usuario.setSolicitudProfesor("RECHAZADA");
                    em.merge(usuario);

                    utx.commit();
                    response.sendRedirect(request.getContextPath() + "/usuario/solicitudes?success=Solicitud rechazada");
                } else {
                    utx.rollback();
                    forwardError(request, response, "Solicitud no encontrada o ya procesada");
                }

            } catch (Exception e) {
                try {
                    utx.rollback();
                } catch (Exception ex) {
                    Log.log(Level.SEVERE, "Error al hacer rollback", ex);
                }
                forwardError(request, response, "Error al procesar la solicitud: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            forwardError(request, response, "ID de usuario inválido");
        }
    }

    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return usuario != null && usuario.getRol() == 0;
        }
        return false;
    }

    // MÉTODO: Verificar permisos para editar usuario
    private boolean tienePermisoParaEditar(HttpServletRequest request, Long idUsuarioEditado) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) {
            return false;
        }

        // Permitir edición si:
        // 1. El usuario es administrador (rol = 0) O
        // 2. El usuario está editando su propio perfil
        return usuarioLogueado.getRol() == 0 || usuarioLogueado.getId().equals(idUsuarioEditado);
    }

    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    // MÉTODO: Buscar usuario por email o DNI excluyendo un ID específico
    private Usuario findByEmailOrDniExcludingId(String email, String dni, Long excludeId) {
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE (LOWER(u.email) = LOWER(:email) OR u.dni = :dni) AND u.id != :excludeId",
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

    // MÉTODO: Buscar usuario por email o DNI
    private Usuario findByEmailOrDni(String email, String dni) {
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) OR u.dni = :dni",
                    Usuario.class);
            query.setParameter("email", email);
            query.setParameter("dni", dni);
            List<Usuario> resultados = query.getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error al buscar usuario existente", e);
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
                    response.sendRedirect(request.getContextPath() + "/instalaciones");
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
