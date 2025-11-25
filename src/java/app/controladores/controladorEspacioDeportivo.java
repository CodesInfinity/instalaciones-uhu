package app.controladores;

import app.modelos.EspacioDeportivo;
import app.modelos.Usuario;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import jakarta.transaction.UserTransaction;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * CONTROLADOR DE ESPACIOS DEPORTIVOS
 * * <p>Servlet encargado de la gestión integral de las instalaciones deportivas (CRUD).</p>
 * * <p><strong>Características principales:</strong></p>
 * <ul>
 * <li>Listado público y privado (panel de administración).</li>
 * <li>Gestión de subida de imágenes mediante {@code @MultipartConfig}.</li>
 * <li>Persistencia de archivos en directorios de despliegue y fuente.</li>
 * <li>Control de transacciones JTA para integridad de datos.</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.0
 */
@MultipartConfig
@WebServlet(name = "ControladorEspacioDeportivo", urlPatterns = {"/instalaciones/*"})
public class controladorEspacioDeportivo extends HttpServlet {

    // ==========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ==========================================

    /** Contexto de persistencia para operaciones JPA. */
    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    
    /** Gestor de transacciones para operaciones de escritura (crear/borrar/editar). */
    @Resource
    private UserTransaction utx;
    
    private static final Logger LOG = Logger.getLogger(controladorEspacioDeportivo.class.getName());

    // ==========================================
    // ENRUTAMIENTO (GET)
    // ==========================================

    /**
     * Maneja las peticiones GET para la navegación y visualización.
     * Actúa como un router basado en la URL solicitada.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/";

        switch (path) {
            // Listado público
            case "/" -> {
                mostrarInstalaciones(request, response);
            }
            
            // Panel de gestión (Solo Admin)
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarPanelAdmin(request, response);
            }
            
            // Formulario de creación (Solo Admin)
            case "/nueva" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para crear instalaciones");
                    return;
                }
                mostrarFormularioNueva(request, response);
            }
            
            // Formulario de edición (Solo Admin)
            case "/editar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para editar instalaciones");
                    return;
                }
                mostrarFormularioEditar(request, response);
            }
            
            // Vista de detalle individual
            case "/detalle" -> {
                mostrarDetalle(request, response);
            }
            
            // Eliminación (Solo Admin)
            case "/borrar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para eliminar instalaciones");
                    return;
                }
                borrarInstalacion(request, response);
            }
            
            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    // ==========================================
    // PROCESAMIENTO (POST)
    // ==========================================

    /**
     * Maneja las peticiones POST, principalmente para el envío de formularios
     * con contenido multimedia (multipart/form-data).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            case "/guardar" -> {
                procesarGuardarInstalacion(request, response);
            }
            default ->
                forwardError(request, response, "Acción no válida");
        }
    }

    // ==========================================
    // MÉTODOS DE NAVEGACIÓN (VISTAS)
    // ==========================================

    /**
     * Recupera y muestra el catálogo público de instalaciones.
     */
    private void mostrarInstalaciones(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<EspacioDeportivo> instalaciones = obtenerTodasLasInstalaciones();
        request.setAttribute("instalaciones", instalaciones);
        
        setLayoutAttributes(request, "Instalaciones Deportivas", 
                "Descubre todos nuestros espacios deportivos disponibles");
        request.setAttribute("pageContent", "../instalaciones/listaInstalaciones.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * Recupera y muestra el panel de administración (tabla de gestión).
     */
    private void mostrarPanelAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<EspacioDeportivo> instalaciones = obtenerTodasLasInstalaciones();
        request.setAttribute("instalaciones", instalaciones);
        
        setLayoutAttributes(request, "Panel de Instalaciones", 
                "Gestiona todas las instalaciones deportivas del sistema");
        request.setAttribute("pageContent", "../instalaciones/panelInstalaciones.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * Prepara y muestra el formulario vacío para una nueva instalación.
     */
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setLayoutAttributes(request, "Nueva Instalación", 
                "Agrega una nueva instalación deportiva al sistema");
        request.setAttribute("pageContent", "../instalaciones/formInstalacion.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * Busca una instalación por ID y muestra el formulario pre-rellenado para edición.
     */
    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        if (idParam != null) {
            EspacioDeportivo instalacion = em.find(EspacioDeportivo.class, Long.parseLong(idParam));
            if (instalacion != null) {
                request.setAttribute("instalacion", instalacion);
                setLayoutAttributes(request, "Editar Instalación", 
                        "Modifica los datos de la instalación deportiva");
                request.setAttribute("pageContent", "../instalaciones/formInstalacion.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            } else {
                forwardError(request, response, "Instalación no encontrada.");
            }
        } else {
            forwardError(request, response, "ID de instalación no proporcionado.");
        }
    }

    /**
     * Muestra la ficha técnica detallada de una instalación.
     */
    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        if (idParam != null) {
            EspacioDeportivo instalacion = em.find(EspacioDeportivo.class, Long.parseLong(idParam));
            if (instalacion != null) {
                request.setAttribute("instalacion", instalacion);
                setLayoutAttributes(request, instalacion.getNombre(), 
                        instalacion.getDescripcion());
                request.setAttribute("pageContent", "../instalaciones/detalleInstalacion.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            } else {
                forwardError(request, response, "Instalación no encontrada.");
            }
        } else {
            forwardError(request, response, "ID de instalación no proporcionado.");
        }
    }

    // ==========================================
    // LÓGICA DE NEGOCIO (CORE)
    // ==========================================

    

    /**
     * Procesa la creación o actualización de una instalación.
     * * <p><strong>Flujo de trabajo:</strong></p>
     * <ol>
     * <li>Recibe parámetros multipart (texto y archivo).</li>
     * <li>Si hay imagen nueva, genera un UUID y la guarda en disco.</li>
     * <li>Guarda la imagen en <em>dos ubicaciones</em>: carpeta {@code build} (despliegue) y carpeta {@code web} (fuente) para persistencia en desarrollo.</li>
     * <li>Valida campos obligatorios.</li>
     * <li>Determina si es INSERT o UPDATE según la presencia del ID.</li>
     * <li>Persiste la entidad en base de datos.</li>
     * </ol>
     */
    private void procesarGuardarInstalacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. OBTENER DATOS
        String idParam = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String tipo = request.getParameter("tipo");
        String ubicacion = request.getParameter("ubicacion");
        String descripcion = request.getParameter("descripcion");
        String imagenUrlActual = request.getParameter("imagenUrlActual"); 

        // 2. GESTIÓN DE ARCHIVOS (SUBIDA DE IMAGEN)
        String urlParaDB = imagenUrlActual; 
        Part filePart = request.getPart("imagen");
        String fileName = filePart.getSubmittedFileName();

        if (fileName != null && !fileName.isEmpty()) {
            try {
                // Generar nombre único (UUID) para evitar colisiones
                String extension = fileName.substring(fileName.lastIndexOf("."));
                String uniqueName = UUID.randomUUID().toString() + extension;
                
                // URL relativa para guardar en BD
                urlParaDB = "/img/instalaciones/temp/" + uniqueName;

                // Definición de rutas físicas
                String appPath = getServletContext().getRealPath("/");
                String sourcePath = appPath.replace("build" + File.separator + "web", "web");

                // Rutas de destino
                Path deployPath = Paths.get(appPath, "img", "instalaciones", "temp");
                Path projectPath = Paths.get(sourcePath, "img", "instalaciones", "temp");

                // Crear directorios si no existen
                if (!Files.exists(deployPath)) Files.createDirectories(deployPath);
                if (!Files.exists(projectPath)) Files.createDirectories(projectPath);

                Path deployFile = deployPath.resolve(uniqueName);
                Path projectFile = projectPath.resolve(uniqueName);

                // Guardar en 'Build' (Servidor activo)
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, deployFile, StandardCopyOption.REPLACE_EXISTING);
                }
                LOG.log(Level.INFO, "Archivo guardado en despliegue: {0}", deployFile);

                // Guardar en 'Source' (Persistencia desarrollo)
                Files.copy(deployFile, projectFile, StandardCopyOption.REPLACE_EXISTING);
                LOG.log(Level.INFO, "Archivo copiado a proyecto: {0}", projectFile);

            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error al subir el archivo", e);
                forwardError(request, response, "Error al guardar la imagen: " + e.getMessage());
                return;
            }
        }

        // 3. VALIDACIÓN Y NORMALIZACIÓN
        try {
            if (nombre == null || tipo == null || ubicacion == null ||
                nombre.trim().isEmpty() || tipo.trim().isEmpty() || ubicacion.trim().isEmpty()) {
                forwardError(request, response, "Nombre, tipo y ubicación son campos obligatorios");
                return;
            }

            String nombreNormalizado = nombre.trim();
            String tipoNormalizado = tipo.trim();
            String ubicacionNormalizada = ubicacion.trim();
            String descripcionNormalizada = descripcion != null ? descripcion.trim() : "";
            String imagenUrlNormalizada = (urlParaDB != null && !urlParaDB.isEmpty()) ? urlParaDB.trim() : null;

            // 4. CREACIÓN O EDICIÓN DE ENTIDAD
            EspacioDeportivo instalacion;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // MODO EDICIÓN
                Long id = Long.parseLong(idParam);
                instalacion = em.find(EspacioDeportivo.class, id);

                if (instalacion == null) {
                    forwardError(request, response, "Instalación no encontrada para editar");
                    return;
                }

                instalacion.setNombre(nombreNormalizado);
                instalacion.setTipo(tipoNormalizado);
                instalacion.setUbicacion(ubicacionNormalizada);
                instalacion.setDescripcion(descripcionNormalizada);
                
                // Actualizar imagen solo si se subió una nueva
                if (imagenUrlNormalizada != null) {
                    instalacion.setImagenUrl(imagenUrlNormalizada);
                }

            } else {
                // MODO CREACIÓN
                instalacion = new EspacioDeportivo(
                    nombreNormalizado,
                    tipoNormalizado,
                    ubicacionNormalizada,
                    descripcionNormalizada,
                    imagenUrlNormalizada
                );
            }

            // 5. PERSISTENCIA
            guardarInstalacion(instalacion);

            response.sendRedirect(request.getContextPath() + "/instalaciones/panel");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar instalación en BBDD", e);
            forwardError(request, response, "Error inesperado al guardar la instalación: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una instalación recibiendo su ID por parámetro GET.
     */
    private void borrarInstalacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                eliminarInstalacion(Long.parseLong(idParam));
                response.sendRedirect(request.getContextPath() + "/instalaciones/panel");
            } catch (Exception e) {
                forwardError(request, response, "Error al eliminar la instalación: " + e.getMessage());
            }
        } else {
            forwardError(request, response, "ID de instalación no proporcionado.");
        }
    }

    // ==========================================
    // CAPA DE ACCESO A DATOS (DAO INTERNO)
    // ==========================================

    /**
     * Obtiene todas las instalaciones ordenadas alfabéticamente.
     * @return Lista de EspacioDeportivo.
     */
    private List<EspacioDeportivo> obtenerTodasLasInstalaciones() {
        TypedQuery<EspacioDeportivo> query = em.createQuery(
            "SELECT e FROM EspacioDeportivo e ORDER BY e.nombre", EspacioDeportivo.class);
        return query.getResultList();
    }

    /**
     * Persiste la entidad en la base de datos utilizando transacciones JTA.
     * Maneja automáticamente {@code persist} (nuevo) o {@code merge} (existente).
     */
    private void guardarInstalacion(EspacioDeportivo instalacion) {
        Long id = instalacion.getId();
        try {
            utx.begin();

            if (id == null) {
                em.persist(instalacion);
                LOG.log(Level.INFO, "Nueva instalación guardada: {0}", instalacion.getNombre());
            } else {
                em.merge(instalacion);
                LOG.log(Level.INFO, "Instalación actualizada: {0}", instalacion.getNombre());
            }

            utx.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Excepción al guardar instalación", e);
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                LOG.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina la entidad de la base de datos.
     */
    private void eliminarInstalacion(Long id) {
        try {
            utx.begin();
            EspacioDeportivo instalacion = em.find(EspacioDeportivo.class, id);
            if (instalacion != null) {
                em.remove(instalacion);
                LOG.log(Level.INFO, "Instalación eliminada: {0}", instalacion.getNombre());
            }
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error al hacer rollback", ex);
            }
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    /**
     * Verifica si el usuario en sesión es administrador (Rol 0).
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
     * Inyecta atributos comunes para el layout (Título y subtítulo de página).
     */
    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    /**
     * Redirige internamente a una vista JSP.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(vista);
        rd.forward(request, response);
    }

    /**
     * Redirige a la página de error estándar con un mensaje personalizado.
     */
    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }
}