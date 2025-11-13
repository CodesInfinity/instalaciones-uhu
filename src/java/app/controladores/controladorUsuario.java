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
 * CONTROLADOR DE USUARIOS
 * 
 * Gestiona todas las operaciones relacionadas con usuarios:
 * - Registro de nuevos usuarios
 * - Autenticación (login)
 * - Gestión de perfiles de usuario
 * - Panel administrativo de usuarios
 * - Solicitudes de promotoría/profesor
 * - Cierre de sesión
 * 
 * @author agustinrodriguez
 * @version 2.1 - Refactorizado con comprobaciones de admin en el enrutador
 */
@WebServlet(name = "controladorUsuario", urlPatterns = {"/usuario/*"})
public class controladorUsuario extends HttpServlet {

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    
    @Resource
    private UserTransaction utx;
    
    // Logger para rastrear eventos importantes
    private static final Logger LOG = Logger.getLogger(controladorUsuario.class.getName());

    /**
     * MÉTODO: doGet
     * Maneja las peticiones GET del controlador
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/login";

        switch (path) {
            // RUTA: /usuario/panel
            // Panel administrativo de usuarios (solo administradores)
            case "/panel" -> {
                // **VERIFICACIÓN DE ADMIN**
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
            
            // RUTA: /usuario/solicitudes
            // Ver solicitudes de profesor/personal
            case "/solicitudes" -> {
                // **VERIFICACIÓN DE ADMIN**
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarSolicitudesPersonal(request, response);
            }
            
            // RUTA: /usuario/editar?id=X
            // Página para editar datos de usuario
            case "/editar" -> {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    // Esta ruta permite admin O el propio usuario (lógica correcta)
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
            
            // RUTA: /usuario/borrar?id=X
            // Eliminar usuario del sistema
            case "/borrar" -> {
                // **VERIFICACIÓN DE ADMIN**
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
            
            // RUTA: /usuario/registro
            // Mostrar formulario de registro
            case "/registro" ->
                forward(request, response, "/WEB-INF/vistas/auth/registro.jsp");
    
            // RUTA: /usuario/login
            // Mostrar formulario de login
            case "/login" ->
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            
            // RUTA: /usuario/logout
            // Cerrar sesión del usuario
            case "/logout" -> {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/");
            }
            
            // Ruta no reconocida
            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    /**
     * MÉTODO: doPost
     * Maneja las peticiones POST del controlador
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            // RUTA: /usuario/save
            // Procesar creación/edición de usuario
            // La lógica de permisos está en procesarGuardarUsuario (admin o self)
            case "/save" -> {
                procesarGuardarUsuario(request, response);
            }
            
            // RUTA: /usuario/login
            // Procesar autenticación
            case "/login" -> {
                procesarLogin(request, response);
            }
            
            // RUTA: /usuario/aprobarSolicitud
            // Aprobar solicitud de profesor
            case "/aprobarSolicitud" -> {
                // **VERIFICACIÓN DE ADMIN**
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos");
                    return;
                }
                procesarAprobarSolicitud(request, response);
            }
            
            // RUTA: /usuario/rechazarSolicitud
            // Rechazar solicitud de profesor
            case "/rechazarSolicitud" -> {
                // **VERIFICACIÓN DE ADMIN**
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

    /**
     * MÉTODO PRIVADO: procesarGuardarUsuario
     * Procesa la creación o edición de un usuario
     * Incluye: validación, verificación de duplicados, encriptación de contraseña
     */
    private void procesarGuardarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener parámetros del formulario
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

            // Determinar rol del usuario
            int rol;
            if (rolParam == null || rolParam.trim().isEmpty()) {
                rol = 1; // Rol por defecto: Estudiante
            } else {
                try {
                    rol = Integer.parseInt(rolParam);
                } catch (NumberFormatException e) {
                    forwardError(request, response, "El formato del rol no es válido");
                    return;
                }
            }
            
            // **INICIO LÓGICA DE PERMISOS**
            // Si estamos editando (idParam existe), verificar permisos
            if (idParam != null && !idParam.trim().isEmpty()) {
                Long id = Long.parseLong(idParam);
                if (!tienePermisoParaEditar(request, id)) {
                    forwardError(request, response, "No tiene permisos para editar este usuario");
                    return;
                }
            }
            // Si es registro (idParam es null), no se requiere permiso especial
            // **FIN LÓGICA DE PERMISOS**

            Usuario usuario;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // MODO EDICIÓN
                Long id = Long.parseLong(idParam);
                usuario = em.find(Usuario.class, id);

                if (usuario == null) {
                    forwardError(request, response, "Usuario no encontrado para editar");
                    return;
                }

                // Verificar si el email o DNI ya existen en otros usuarios
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

                // Actualizar datos
                usuario.setDni(dniNormalizado);
                usuario.setNombre(nombreNormalizado);
                usuario.setEmail(emailNormalizado);
                
                // Solo un admin puede cambiar el rol
                if(esAdministrador(request)) {
                    usuario.setRol(rol);
                }

                // Solo actualizar contraseña si se proporciona una nueva
                if (password != null && !password.trim().isEmpty()) {
                    if (password.length() < 6) {
                        forwardError(request, response, "La contraseña debe tener al menos 6 caracteres");
                        return;
                    }
                    String hashedPassword = hashPassword(password);
                    usuario.setPassword(hashedPassword);
                }

            } else {
                // MODO REGISTRO
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
                
                // Si no es un admin, el rol es 1 (Estudiante)
                // Si es un admin creando un usuario, se respeta el rol del formulario
                int rolFinal = esAdministrador(request) ? rol : 1;
                
                usuario = new Usuario(
                        dniNormalizado,
                        nombreNormalizado,
                        emailNormalizado,
                        hashedPassword,
                        rolFinal
                );

                // Procesar solicitud de profesor si se envió
                if ("true".equals(solicitarProfesor)) {
                    usuario.setSolicitudProfesor("PENDIENTE");
                }
            }

            // Guardar usuario en la base de datos
            save(usuario);

            // Redirigir según el contexto
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("usuario") != null) {
                Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

                // Si es edición del propio perfil, actualizar sesión
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Long idEditado = Long.parseLong(idParam);
                    if (usuarioLogueado.getId().equals(idEditado)) {
                        session.setAttribute("usuario", usuario);
                    }
                }

                // Redirigir según rol
                if (usuarioLogueado.getRol() == 0) {
                    response.sendRedirect(request.getContextPath() + "/usuario/panel");
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }
            } else {
                // Usuario no logueado (registro): mostrar login
                request.setAttribute("success", "Usuario registrado correctamente. Puede iniciar sesión.");
                forward(request, response, "/WEB-INF/vistas/auth/login.jsp");
            }

        } catch (Exception e) {
            forwardError(request, response, "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: mostrarSolicitudesPersonal
     * Muestra la lista de solicitudes de promoción a profesor
     * **La verificación de admin se hace en doGet**
     */
    private void mostrarSolicitudesPersonal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

    /**
     * MÉTODO PRIVADO: procesarAprobarSolicitud
     * Aprueba una solicitud de profesor/personal
     * **La verificación de admin se hace en doPost**
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
                    // Cambiar rol a profesor (rol = 2) y marcar solicitud como aprobada
                    usuario.setRol(2);
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
     * MÉTODO PRIVADO: procesarRechazarSolicitud
     * Rechaza una solicitud de profesor/personal
     * **La verificación de admin se hace en doPost**
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
                    // Marcar solicitud como rechazada, mantener rol actual
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
     * MÉTODO PRIVADO: procesarLogin
     * Procesa la autenticación de un usuario
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

            // Utilizar servicio de autenticación
            AuthService authService = new AuthService();
            authService.em = em;
            Usuario usuario = authService.autenticarPorEmail(email, password);

            if (usuario != null) {
                LOG.log(Level.INFO, "Login exitoso para usuario: {0}, rol: {1}",
                        new Object[]{usuario.getNombre(), usuario.getRol()});

                // Crear sesión y almacenar usuario
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);

                // Redirigir según rol
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

    // ===== MÉTODOS DE ACCESO A DATOS =====

    /**
     * MÉTODO PRIVADO: obtenerUsuarios
     * Obtiene la lista de todos los usuarios del sistema
     */
    private List<Usuario> obtenerUsuarios() {
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        return query.getResultList();
    }

    /**
     * MÉTODO PRIVADO: findByEmailOrDniExcludingId
     * Busca un usuario por email o DNI excluyendo un ID específico
     * Utilizado para verificar duplicados al editar
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
     * MÉTODO PRIVADO: findByEmailOrDni
     * Busca un usuario por email o DNI
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
     * MÉTODO PRIVADO: save
     * Persiste un usuario en la base de datos
     * Detecta automáticamente si es creación o actualización
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
     * MÉTODO PRIVADO: borrarUsuario
     * Elimina un usuario del sistema
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

    // ===== MÉTODOS AUXILIARES =====

    /**
     * MÉTODO PRIVADO: esAdministrador
     * Verifica si el usuario logueado tiene permisos de administrador (rol = 0)
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
     * MÉTODO PRIVADO: tienePermisoParaEditar
     * Verifica si el usuario tiene permisos para editar otro usuario
     * Permisos: ser administrador O estar editando el propio perfil
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
     * MÉTODO PRIVADO: setLayoutAttributes
     * Establece los atributos necesarios para el layout principal
     */
    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    /**
     * MÉTODO PRIVADO: forward
     * Realiza un forward a una JSP específica
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(vista);
        rd.forward(request, response);
    }

    /**
     * MÉTODO PRIVADO: forwardError
     * Muestra la página de error con un mensaje específico
     */
    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }

    /**
     * MÉTODO PRIVADO: hashPassword
     * Encripta una contraseña usando el algoritmo MD5
     * 
     * ⚠️ ADVERTENCIA: MD5 es considerado INSECURE para contraseñas porque:
     * - Es vulnerable a ataques de colisión
     * - Es muy rápido (permite brute force attacks)
     * - No usa salt (misma contraseña = mismo hash siempre)
     * 
     * @param password La contraseña en texto plano a encriptar
     * @return String El hash MD5 de la contraseña en mayúsculas
     * @throws NoSuchAlgorithmException Si el algoritmo MD5 no está disponible en el sistema
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return myHash;
    }
    
}