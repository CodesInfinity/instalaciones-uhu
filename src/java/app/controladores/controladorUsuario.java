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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * CONTROLADOR DE GESTIÓN DE USUARIOS
 * * <p>Este Servlet actúa como el controlador central para todas las operaciones relacionadas con 
 * la entidad {@link Usuario}. Implementa el patrón MVC gestionando las peticiones HTTP, 
 * interactuando con la capa de persistencia y despachando a las vistas JSP correspondientes.</p>
 * * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 * <li>Autenticación y autorización (Login/Logout).</li>
 * <li>CRUD completo de usuarios (Crear, Leer, Actualizar, Borrar).</li>
 * <li>Gestión de roles y permisos (Admin vs Usuario estándar).</li>
 * <li>Flujo de aprobación para solicitudes de rol 'Profesor'.</li>
 * <li>Endpoints AJAX para validaciones asíncronas (Email/DNI).</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.1
 * @see app.modelos.Usuario
 */
@WebServlet(name = "controladorUsuario", urlPatterns = {"/usuario/*"})
public class controladorUsuario extends HttpServlet {

    // ==========================================
    // INYECCIÓN DE DEPENDENCIAS Y RECURSOS
    // ==========================================

    /**
     * Contexto de persistencia para operaciones con la base de datos (JPA).
     */
    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;

    /**
     * Gestor de transacciones (JTA) para controlar commit/rollback manualmente
     * cuando se requiere lógica transaccional compleja.
     */
    @Resource
    private UserTransaction utx;

    /**
     * Logger para registro de eventos, auditoría y depuración.
     */
    private static final Logger LOG = Logger.getLogger(controladorUsuario.class.getName());

    // ==========================================
    // GESTIÓN DE PETICIONES HTTP (ROUTING)
    // ==========================================

    /**
     * Maneja las peticiones GET. Actúa como enrutador principal para la navegación
     * y visualización de datos.
     * * @param request La solicitud HTTP.
     * @param response La respuesta HTTP.
     * @throws ServletException Si ocurre un error en el servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la ruta relativa (ej: /panel, /login)
        String path = request.getPathInfo() != null ? request.getPathInfo() : "/login";

        // Detección de peticiones AJAX
        String ajax = request.getParameter("ajax");
        boolean esAjax = "true".equals(ajax);

        switch (path) {
            // -----------------------------------------------------------------
            // RUTA: /usuario/panel (ADMINISTRACIÓN)
            // Lista todos los usuarios. Requiere rol de Administrador.
            // -----------------------------------------------------------------
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }

                List<Usuario> usuarios = obtenerUsuarios();
                request.setAttribute("usuarios", usuarios);
                setLayoutAttributes(request, "Panel de Usuarios",
                        "Gestiona todos los usuarios registrados en el sistema de instalaciones deportivas");
                request.setAttribute("pageContent", "../admin/panelUsuarios.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            }

            // -----------------------------------------------------------------
            // RUTA: /usuario/solicitudes (ADMINISTRACIÓN)
            // Bandeja de entrada para aprobaciones de rol profesor.
            // -----------------------------------------------------------------
            case "/solicitudes" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarSolicitudesPersonal(request, response);
            }

            // -----------------------------------------------------------------
            // RUTA: /usuario/editar (EDICIÓN DE PERFIL)
            // Carga el formulario con datos existentes.
            // Permite edición propia o edición por administrador.
            // -----------------------------------------------------------------
            case "/editar" -> {
                String idParam = request.getParameter("id");
                Long usuarioId;

                // Lógica de decisión: ¿Edito a otro o a mí mismo?
                if (idParam != null) {
                    // Intento de editar a otro usuario
                    usuarioId = Long.parseLong(idParam);
                    if (!tienePermisoParaEditar(request, usuarioId)) {
                        forwardError(request, response, "No tiene permisos para editar este usuario.");
                        return;
                    }
                } else {
                    // Edición del perfil propio (Sesión activa)
                    HttpSession session = request.getSession();
                    Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
                    if (usuarioSesion == null) {
                        forwardError(request, response, "No hay usuario en sesión.");
                        return;
                    }
                    usuarioId = usuarioSesion.getId();
                }

                // Recuperación de la entidad
                Usuario usuario = em.find(Usuario.class, usuarioId);
                if (usuario == null) {
                    forwardError(request, response, "Usuario no encontrado.");
                    return;
                }

                request.setAttribute("usuario", usuario);
                setLayoutAttributes(request, "Editar Usuario",
                        "Modifica los datos del usuario seleccionado");
                request.setAttribute("pageContent", "../admin/editarUsuario.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            }

            // -----------------------------------------------------------------
            // RUTA: /usuario/borrar (ELIMINACIÓN)
            // Requiere rol de Administrador.
            // -----------------------------------------------------------------
            case "/borrar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para eliminar usuarios.");
                    return;
                }

                String idParam = request.getParameter("id");
                if (idParam != null) {
                    borrarUsuario(Long.parseLong(idParam));
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    forwardError(request, response, "ID de usuario no proporcionado.");
                }
            }

            // -----------------------------------------------------------------
            // RUTAS PÚBLICAS Y DE AUTENTICACIÓN
            // -----------------------------------------------------------------
            case "/registro" ->
                forward(request, response, "/WEB-INF/vistas/auth/registro.jsp");

            case "/login" ->
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");

            case "/logout" -> {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate(); // Destruye la sesión del servidor
                }
                response.sendRedirect(request.getContextPath() + "/");
            }

            // -----------------------------------------------------------------
            // RUTAS AJAX (VALIDACIONES API)
            // Devuelven JSON puro, no vistas JSP.
            // -----------------------------------------------------------------
            case "/validar-email" -> {
                if (!esAjax) {
                    forwardError(request, response, "Método no permitido");
                    return;
                }
                validarEmailAjax(request, response);
            }

            case "/validar-dni" -> {
                if (!esAjax) {
                    forwardError(request, response, "Método no permitido");
                    return;
                }
                validarDniAjax(request, response);
            }

            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    /**
     * Maneja las peticiones POST. Procesa formularios y cambios de estado en la base de datos.
     * * @param request La solicitud HTTP.
     * @param response La respuesta HTTP.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            // Procesa tanto el registro nuevo como la edición de perfil
            case "/save" -> {
                procesarGuardarUsuario(request, response);
            }

            // Procesa las credenciales de acceso
            case "/login" -> {
                procesarLogin(request, response);
            }

            // Gestión de solicitudes de rol (Solo Admin)
            case "/aprobarSolicitud" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos");
                    return;
                }
                procesarAprobarSolicitud(request, response);
            }

            case "/rechazarSolicitud" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos");
                    return;
                }
                procesarRechazarSolicitud(request, response);
            }

            default ->
                forwardError(request, response, "Acción no válida");
        }
    }

    // ==========================================
    // LÓGICA DE NEGOCIO PRINCIPAL
    // ==========================================

    

    /**
     * Orquesta el proceso de creación o actualización de un usuario.
     * <p>Pasos que realiza:</p>
     * <ol>
     * <li>Validación de parámetros obligatorios.</li>
     * <li>Normalización de datos (trim, lowercase).</li>
     * <li>Verificación de permisos de seguridad (¿Quién intenta guardar?).</li>
     * <li>Verificación de duplicados (Email/DNI únicos).</li>
     * <li>Hashing de contraseña (si aplica).</li>
     * <li>Persistencia en base de datos.</li>
     * <li>Redirección contextual post-guardado.</li>
     * </ol>
     */
    private void procesarGuardarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Extracción de parámetros
        String idParam = request.getParameter("id");
        String dni = request.getParameter("dni");
        String nombre = request.getParameter("nombre");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rolParam = request.getParameter("rol");
        String solicitarProfesor = request.getParameter("solicitarProfesor");

        try {
            // 2. Validaciones básicas
            if (dni == null || nombre == null || email == null
                    || dni.trim().isEmpty() || nombre.trim().isEmpty() || email.trim().isEmpty()) {
                forwardError(request, response, "Todos los campos obligatorios deben ser completados");
                return;
            }

            // 3. Normalización
            String emailNormalizado = email.trim().toLowerCase();
            String dniNormalizado = dni.trim();
            String nombreNormalizado = nombre.trim();

            if (!emailNormalizado.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                forwardError(request, response, "El formato del email no es válido");
                return;
            }

            // 4. Determinación del Rol
            int rol;
            if (rolParam == null || rolParam.trim().isEmpty()) {
                rol = 1; // Default: Estudiante
            } else {
                try {
                    rol = Integer.parseInt(rolParam);
                } catch (NumberFormatException e) {
                    forwardError(request, response, "El formato del rol no es válido");
                    return;
                }
            }

            // 5. Verificación de Seguridad (Autorización)
            // Si es edición, ¿tiene permiso el usuario actual?
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                if (!tienePermisoParaEditar(request, id)) {
                    forwardError(request, response, "No tiene permisos para editar este usuario");
                    return;
                }
            }

            Usuario usuario;

            // -------------------------------------------------------
            // RAMA: EDICIÓN (UPDATE)
            // -------------------------------------------------------
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                usuario = em.find(Usuario.class, id);

                if (usuario == null) {
                    forwardError(request, response, "Usuario no encontrado para editar");
                    return;
                }

                // Check Duplicados (excluyendo el propio usuario)
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

                // Actualización de campos
                usuario.setDni(dniNormalizado);
                usuario.setNombre(nombreNormalizado);
                usuario.setEmail(emailNormalizado);

                // Seguridad: Solo admin puede elevar privilegios vía POST
                if (esAdministrador(request)) {
                    usuario.setRol(rol);
                }

                // Seguridad: Solo hashear si la contraseña cambió
                if (password != null && !password.trim().isEmpty()) {
                    if (password.length() < 6) {
                        forwardError(request, response, "La contraseña debe tener al menos 6 caracteres");
                        return;
                    }
                    String hashedPassword = hashPassword(password);
                    usuario.setPassword(hashedPassword);
                }

            } 
            // -------------------------------------------------------
            // RAMA: REGISTRO NUEVO (CREATE)
            // -------------------------------------------------------
            else {
                if (password == null || password.trim().isEmpty()) {
                    forwardError(request, response, "La contraseña es requerida para el registro");
                    return;
                }

                if (password.length() < 6) {
                    forwardError(request, response, "La contraseña debe tener al menos 6 caracteres");
                    return;
                }

                // Check Duplicados Global
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

                String hashedPassword = hashPassword(password);

                // Seguridad: Si no es admin, forzar rol estudiante (1)
                int rolFinal = esAdministrador(request) ? rol : 1;

                usuario = new Usuario(
                        dniNormalizado,
                        nombreNormalizado,
                        emailNormalizado,
                        hashedPassword,
                        rolFinal
                );

                // Gestionar checkbox de solicitud de rol
                if ("true".equals(solicitarProfesor)) {
                    usuario.setSolicitudProfesor("PENDIENTE");
                }
            }

            // 6. Persistencia
            save(usuario);

            // 7. Gestión de Sesión y Redirección
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("usuario") != null) {
                Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

                // Si edité mi propio perfil, actualizo el objeto en sesión para reflejar cambios inmediatos
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Long idEditado = Long.parseLong(idParam);
                    if (usuarioLogueado.getId().equals(idEditado)) {
                        session.setAttribute("usuario", usuario);
                    }
                }

                // Navegación post-guardado
                if (usuarioLogueado.getRol() == 0) {
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }
            } else {
                // Caso Registro Público: Enviar al login
                request.setAttribute("success", "Usuario registrado correctamente. Puede iniciar sesión.");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }

        } catch (Exception e) {
            forwardError(request, response, "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Muestra la vista de gestión de solicitudes pendientes.
     * Solo accesible vía {@code doGet} por administradores.
     */
    private void mostrarSolicitudesPersonal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Usuario> solicitudesPendientes = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.solicitudProfesor = 'PENDIENTE'", Usuario.class)
                .getResultList();

        request.setAttribute("solicitudes", solicitudesPendientes);
        setLayoutAttributes(request, "Solicitudes de personal",
                "Gestiona las solicitudes de usuarios que quieren ser personal de la UHU");
        request.setAttribute("pageContent", "../admin/solicitudesPersonal.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    

    /**
     * Procesa la aprobación de una solicitud de profesor.
     * <p>Efectos:</p>
     * <ul>
     * <li>Cambia el rol del usuario a '2' (Profesor).</li>
     * <li>Actualiza el estado de solicitud a 'APROBADA'.</li>
     * </ul>
     */
    private void procesarAprobarSolicitud(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usuarioIdParam = request.getParameter("usuarioId");
        try {
            Long usuarioId = Long.parseLong(usuarioIdParam);

            try {
                utx.begin();
                Usuario usuario = em.find(Usuario.class, usuarioId);

                if (usuario != null && "PENDIENTE".equals(usuario.getSolicitudProfesor())) {
                    usuario.setRol(2); // Rol Profesor
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
                    LOG.log(Level.SEVERE, "Error al hacer rollback", ex);
                }
                forwardError(request, response, "Error al procesar la solicitud: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            forwardError(request, response, "ID de usuario inválido");
        }
    }

    /**
     * Procesa el rechazo de una solicitud de profesor.
     * <p>Efectos:</p>
     * <ul>
     * <li>Mantiene el rol actual del usuario.</li>
     * <li>Actualiza el estado de solicitud a 'RECHAZADA'.</li>
     * </ul>
     */
    private void procesarRechazarSolicitud(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usuarioIdParam = request.getParameter("usuarioId");
        try {
            Long usuarioId = Long.parseLong(usuarioIdParam);

            try {
                utx.begin();
                Usuario usuario = em.find(Usuario.class, usuarioId);

                if (usuario != null && "PENDIENTE".equals(usuario.getSolicitudProfesor())) {
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
                    LOG.log(Level.SEVERE, "Error al hacer rollback", ex);
                }
                forwardError(request, response, "Error al procesar la solicitud: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            forwardError(request, response, "ID de usuario inválido");
        }
    }

    /**
     * Gestiona la autenticación del usuario contra el servicio {@link AuthService}.
     * Crea la sesión HTTP si las credenciales son válidas.
     */
    private void procesarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            LOG.log(Level.INFO, "Intento de login con email: {0}", email);

            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                request.setAttribute("error", "Email y contraseña son requeridos");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
                return;
            }

            // Delegación de lógica al servicio de autenticación
            AuthService authService = new AuthService();
            authService.em = em; // Inyección manual necesaria si AuthService no es un EJB gestionado
            Usuario usuario = authService.autenticarPorEmail(email, password);

            if (usuario != null) {
                LOG.log(Level.INFO, "Login exitoso para usuario: {0}, rol: {1}",
                        new Object[]{usuario.getNombre(), usuario.getRol()});

                // Creación de Sesión
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);

                // Redirección basada en Rol
                if (usuario.getRol() == 0) {
                    LOG.log(Level.INFO, "Redirigiendo admin a panel");
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    LOG.log(Level.INFO, "Redirigiendo usuario regular a inicio");
                    response.sendRedirect(request.getContextPath() + "/instalaciones");
                }
            } else {
                LOG.log(Level.WARNING, "Login fallido para email: {0}", email);
                request.setAttribute("error", "Email o contraseña incorrectos");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en el login", e);
            forwardError(request, response, "Error en el login: " + e.getMessage());
        }
    }

    // ==========================================
    // CAPA DE ACCESO A DATOS (DAO INTERNO)
    // ==========================================

    /**
     * Recupera todos los usuarios registrados.
     * @return Lista completa de usuarios.
     */
    private List<Usuario> obtenerUsuarios() {
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        return query.getResultList();
    }

    /**
     * Busca colisiones de Email o DNI excluyendo un ID específico.
     * Esencial para validaciones durante la edición de perfil.
     * * @param email Email a comprobar.
     * @param dni DNI a comprobar.
     * @param excludeId ID del usuario que se está editando (para no detectarse a sí mismo).
     * @return El usuario encontrado o null.
     */
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
            LOG.log(Level.SEVERE, "Error al buscar usuario existente excluyendo ID", e);
            return null;
        }
    }

    /**
     * Busca colisiones de Email o DNI para nuevos registros.
     * * @param email Email a comprobar.
     * @param dni DNI a comprobar.
     * @return El usuario encontrado o null.
     */
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
            LOG.log(Level.SEVERE, "Error al buscar usuario existente", e);
            return null;
        }
    }

    /**
     * Persiste o actualiza un usuario utilizando una transacción JTA.
     * Detecta automáticamente si debe ejecutar {@code persist} (nuevo) o {@code merge} (existente).
     * * @param usuario La entidad a guardar.
     * @throws RuntimeException Si falla la transacción.
     */
    public void save(Usuario usuario) {
        Long id = usuario.getId();
        try {
            utx.begin();

            if (id == null) {
                em.persist(usuario);
                LOG.log(Level.INFO, "Nuevo usuario guardado");
            } else {
                em.merge(usuario);
                LOG.log(Level.INFO, "Usuario {0} actualizado", id);
            }

            utx.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Excepción al guardar usuario", e);
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                LOG.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina un usuario por su ID dentro de una transacción.
     * @param id ID del usuario a borrar.
     */
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
                LOG.log(Level.SEVERE, null, ex);
            }
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y DE SEGURIDAD
    // ==========================================

    /**
     * Comprueba si la sesión actual pertenece a un Administrador.
     * @param request La petición HTTP.
     * @return true si el usuario tiene rol 0 (Admin).
     */
    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return usuario != null && usuario.getRol() == 0;
        }
        return false;
    }

    /**
     * Lógica de autorización para edición.
     * Permite la operación si el usuario es Admin O si se está editando a sí mismo.
     * * @param request La petición con la sesión activa.
     * @param idUsuarioEditado El ID del perfil que se intenta modificar.
     * @return true si tiene permiso.
     */
    private boolean tienePermisoParaEditar(HttpServletRequest request, Long idUsuarioEditado) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) {
            return false;
        }

        // Permitir si es administrador o está editando su propio perfil
        return usuarioLogueado.getRol() == 0 || usuarioLogueado.getId().equals(idUsuarioEditado);
    }

    /**
     * Helper para inyectar atributos comunes del layout JSP (títulos, migas de pan).
     */
    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    /**
     * Helper para redirigir internamente (Forward) a una vista JSP.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(vista);
        rd.forward(request, response);
    }

    /**
     * Helper para redirigir a la página de error estandarizada.
     */
    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }

    /**
     * Genera un hash MD5 de la contraseña.
     * * <p><strong>⚠️ ADVERTENCIA DE SEGURIDAD:</strong> MD5 se considera criptográficamente roto.
     * Se mantiene por compatibilidad con sistemas legacy. Para nuevos desarrollos,
     * migrar a BCrypt o Argon2.</p>
     * * @param password Contraseña en texto plano.
     * @return Hash hexadecimal en mayúsculas.
     * @throws NoSuchAlgorithmException Si el proveedor de seguridad no soporta MD5.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return myHash;
    }

    // ==========================================
    // ENDPOINTS AJAX (JSON RESPONSES)
    // ==========================================

    /**
     * Valida la disponibilidad de un email vía AJAX.
     * Escribe un JSON {@code {"valido": boolean, "mensaje": string}} en la respuesta.
     */
    private void validarEmailAjax(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String idParam = request.getParameter("id"); // Para excluir el propio usuario al editar

        if (email == null || email.trim().isEmpty()) {
            response.getWriter().write("{\"valido\": false, \"mensaje\": \"Email requerido\"}");
            return;
        }

        try {
            String emailNormalizado = email.trim().toLowerCase();

            // Validar formato
            if (!emailNormalizado.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.getWriter().write("{\"valido\": false, \"mensaje\": \"Formato de email inválido\"}");
                return;
            }

            // Buscar en BD
            Usuario usuarioExistente;
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                TypedQuery<Usuario> query = em.createQuery(
                        "SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) AND u.id != :id",
                        Usuario.class);
                query.setParameter("email", emailNormalizado);
                query.setParameter("id", id);
                List<Usuario> resultados = query.getResultList();
                usuarioExistente = resultados.isEmpty() ? null : resultados.get(0);
            } else {
                TypedQuery<Usuario> query = em.createQuery(
                        "SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)",
                        Usuario.class);
                query.setParameter("email", emailNormalizado);
                List<Usuario> resultados = query.getResultList();
                usuarioExistente = resultados.isEmpty() ? null : resultados.get(0);
            }

            if (usuarioExistente != null) {
                response.getWriter().write("{\"valido\": false, \"mensaje\": \"Este email ya está registrado\"}");
            } else {
                response.getWriter().write("{\"valido\": true, \"mensaje\": \"Email disponible\"}");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al validar email", e);
            response.getWriter().write("{\"valido\": false, \"mensaje\": \"Error al validar email\"}");
        }
    }

    /**
     * Valida la disponibilidad de un DNI vía AJAX.
     * Escribe un JSON {@code {"valido": boolean, "mensaje": string}} en la respuesta.
     */
    private void validarDniAjax(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String dni = request.getParameter("dni");
        String idParam = request.getParameter("id"); // Para excluir el propio usuario al editar

        if (dni == null || dni.trim().isEmpty()) {
            response.getWriter().write("{\"valido\": false, \"mensaje\": \"DNI requerido\"}");
            return;
        }

        try {
            String dniNormalizado = dni.trim();

            // Buscar en BD
            Usuario usuarioExistente;
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                TypedQuery<Usuario> query = em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.dni = :dni AND u.id != :id",
                        Usuario.class);
                query.setParameter("dni", dniNormalizado);
                query.setParameter("id", id);
                List<Usuario> resultados = query.getResultList();
                usuarioExistente = resultados.isEmpty() ? null : resultados.get(0);
            } else {
                TypedQuery<Usuario> query = em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.dni = :dni",
                        Usuario.class);
                query.setParameter("dni", dniNormalizado);
                List<Usuario> resultados = query.getResultList();
                usuarioExistente = resultados.isEmpty() ? null : resultados.get(0);
            }

            if (usuarioExistente != null) {
                response.getWriter().write("{\"valido\": false, \"mensaje\": \"Este DNI ya está registrado\"}");
            } else {
                response.getWriter().write("{\"valido\": true, \"mensaje\": \"DNI disponible\"}");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al validar DNI", e);
            response.getWriter().write("{\"valido\": false, \"mensaje\": \"Error al validar DNI\"}");
        }
    }
}