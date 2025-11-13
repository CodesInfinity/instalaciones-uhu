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
 * 
 * Gestiona todas las operaciones relacionadas con las instalaciones deportivas:
 * - Listado de instalaciones disponibles
 * - Panel administrativo de gestión
 * - Creación y edición de instalaciones
 * - Visualización de detalles
 * - Eliminación de instalaciones
 * - Manejo de subida de imágenes
 * 
 * @author agustinrodriguez
 * @version 2.0 - Refactorizado y comentado
 */
@MultipartConfig
@WebServlet(name = "ControladorEspacioDeportivo", urlPatterns = {"/instalaciones/*"})
public class controladorEspacioDeportivo extends HttpServlet {

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    
    @Resource
    private UserTransaction utx;
    
    // Logger para rastrear eventos importantes
    private static final Logger LOG = Logger.getLogger(controladorEspacioDeportivo.class.getName());

    /**
     * MÉTODO: doGet
     * Maneja las peticiones GET del controlador
     * Utiliza switch para enrutar a diferentes métodos según el path
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/";

        switch (path) {
            // RUTA: /instalaciones/
            // Mostrar lista pública de todas las instalaciones
            case "/" -> {
                mostrarInstalaciones(request, response);
            }
            
            // RUTA: /instalaciones/panel
            // Panel administrativo - requiere permisos de admin
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarPanelAdmin(request, response);
            }
            
            // RUTA: /instalaciones/nueva
            // Formulario para crear nueva instalación
            case "/nueva" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para crear instalaciones");
                    return;
                }
                mostrarFormularioNueva(request, response);
            }
            
            // RUTA: /instalaciones/editar?id=X
            // Formulario para editar instalación existente
            case "/editar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para editar instalaciones");
                    return;
                }
                mostrarFormularioEditar(request, response);
            }
            
            // RUTA: /instalaciones/detalle?id=X
            // Página de detalles de una instalación específica
            case "/detalle" -> {
                mostrarDetalle(request, response);
            }
            
            // RUTA: /instalaciones/borrar?id=X
            // Eliminar una instalación
            case "/borrar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para eliminar instalaciones");
                    return;
                }
                borrarInstalacion(request, response);
            }
            
            // Ruta no reconocida
            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    /**
     * MÉTODO: doPost
     * Maneja las peticiones POST del controlador
     * Principalmente para procesar formularios de creación/edición
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            // RUTA: /instalaciones/guardar
            // Procesar formulario de creación/edición de instalación
            case "/guardar" -> {
                procesarGuardarInstalacion(request, response);
            }
            default ->
                forwardError(request, response, "Acción no válida");
        }
    }

    /**
     * MÉTODO PRIVADO: mostrarInstalaciones
     * Obtiene todas las instalaciones y las muestra en la vista pública
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
     * MÉTODO PRIVADO: mostrarPanelAdmin
     * Carga el panel administrativo con todas las instalaciones
     * Solo accesible para administradores (rol = 0)
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
     * MÉTODO PRIVADO: mostrarFormularioNueva
     * Muestra el formulario vacío para crear una nueva instalación
     */
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setLayoutAttributes(request, "Nueva Instalación", 
                "Agrega una nueva instalación deportiva al sistema");
        request.setAttribute("pageContent", "../instalaciones/formInstalacion.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * MÉTODO PRIVADO: mostrarFormularioEditar
     * Carga los datos de una instalación y muestra el formulario para editarla
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
     * MÉTODO PRIVADO: mostrarDetalle
     * Muestra la página de detalles de una instalación específica
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

    /**
     * MÉTODO PRIVADO: procesarGuardarInstalacion
     * Procesa el formulario de creación/edición de instalación
     * Incluye: validación de datos, manejo de subida de archivo, persistencia en BD
     */
    private void procesarGuardarInstalacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ===== FASE 1: OBTENER PARÁMETROS DEL FORMULARIO =====
        String idParam = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String tipo = request.getParameter("tipo");
        String ubicacion = request.getParameter("ubicacion");
        String descripcion = request.getParameter("descripcion");
        String imagenUrlActual = request.getParameter("imagenUrlActual"); 

        // ===== FASE 2: PROCESAR SUBIDA DE ARCHIVO =====
        String urlParaDB = imagenUrlActual; 
        Part filePart = request.getPart("imagen");
        String fileName = filePart.getSubmittedFileName();

        if (fileName != null && !fileName.isEmpty()) {
            try {
                // Crear nombre de archivo único usando UUID para evitar conflictos
                String extension = fileName.substring(fileName.lastIndexOf("."));
                String uniqueName = UUID.randomUUID().toString() + extension;
                
                // URL relativa que se guardará en la BD
                urlParaDB = "/img/instalaciones/temp/" + uniqueName;

                // Obtener rutas del servidor
                String appPath = getServletContext().getRealPath("/");
                String sourcePath = appPath.replace("build" + File.separator + "web", "web");

                // Ruta de despliegue: build/web/img/instalaciones/temp
                Path deployPath = Paths.get(appPath, "img", "instalaciones", "temp");

                // Ruta de proyecto: web/img/instalaciones/temp
                Path projectPath = Paths.get(sourcePath, "img", "instalaciones", "temp");

                // Crear directorios si no existen
                if (!Files.exists(deployPath)) Files.createDirectories(deployPath);
                if (!Files.exists(projectPath)) Files.createDirectories(projectPath);

                // Definir archivos de destino
                Path deployFile = deployPath.resolve(uniqueName);
                Path projectFile = projectPath.resolve(uniqueName);

                // Guardar archivo en carpeta de despliegue
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, deployFile, StandardCopyOption.REPLACE_EXISTING);
                }
                LOG.log(Level.INFO, "Archivo guardado en despliegue: {0}", deployFile);

                // Copiar archivo a carpeta del proyecto
                Files.copy(deployFile, projectFile, StandardCopyOption.REPLACE_EXISTING);
                LOG.log(Level.INFO, "Archivo copiado a proyecto: {0}", projectFile);

            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error al subir el archivo", e);
                forwardError(request, response, "Error al guardar la imagen: " + e.getMessage());
                return;
            }
        }

        // ===== FASE 3: VALIDAR Y NORMALIZAR DATOS =====
        try {
            // Validar campos requeridos
            if (nombre == null || tipo == null || ubicacion == null ||
                nombre.trim().isEmpty() || tipo.trim().isEmpty() || ubicacion.trim().isEmpty()) {
                forwardError(request, response, "Nombre, tipo y ubicación son campos obligatorios");
                return;
            }

            // Normalizar datos (trim y sanitizar)
            String nombreNormalizado = nombre.trim();
            String tipoNormalizado = tipo.trim();
            String ubicacionNormalizada = ubicacion.trim();
            String descripcionNormalizada = descripcion != null ? descripcion.trim() : "";
            String imagenUrlNormalizada = (urlParaDB != null && !urlParaDB.isEmpty()) ? urlParaDB.trim() : null;

            // ===== FASE 4: DETERMINAR MODO (CREAR O EDITAR) =====
            EspacioDeportivo instalacion;

            if (idParam != null && !idParam.trim().isEmpty()) {
                // MODO EDICIÓN: Actualizar instalación existente
                Long id = Long.parseLong(idParam);
                instalacion = em.find(EspacioDeportivo.class, id);

                if (instalacion == null) {
                    forwardError(request, response, "Instalación no encontrada para editar");
                    return;
                }

                // Actualizar campos
                instalacion.setNombre(nombreNormalizado);
                instalacion.setTipo(tipoNormalizado);
                instalacion.setUbicacion(ubicacionNormalizada);
                instalacion.setDescripcion(descripcionNormalizada);
                
                // Solo actualizar imagen si se proporcionó una nueva
                if (imagenUrlNormalizada != null) {
                    instalacion.setImagenUrl(imagenUrlNormalizada);
                }

            } else {
                // MODO CREACIÓN: Crear nueva instalación
                instalacion = new EspacioDeportivo(
                    nombreNormalizado,
                    tipoNormalizado,
                    ubicacionNormalizada,
                    descripcionNormalizada,
                    imagenUrlNormalizada
                );
            }

            // ===== FASE 5: GUARDAR EN BASE DE DATOS =====
            guardarInstalacion(instalacion);

            // Redirigir al panel de administración
            response.sendRedirect(request.getContextPath() + "/instalaciones/panel");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar instalación en BBDD", e);
            forwardError(request, response, "Error inesperado al guardar la instalación: " + e.getMessage());
        }
    }
    
    /**
     * MÉTODO PRIVADO: borrarInstalacion
     * Elimina una instalación del sistema
     * Nota: La eliminación del archivo de imagen podría implementarse aquí
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

    // ===== MÉTODOS DE ACCESO A DATOS (DATA ACCESS) =====

    /**
     * MÉTODO PRIVADO: obtenerTodasLasInstalaciones
     * Retrieves all sports facilities from the database ordered by name
     */
    private List<EspacioDeportivo> obtenerTodasLasInstalaciones() {
        TypedQuery<EspacioDeportivo> query = em.createQuery(
            "SELECT e FROM EspacioDeportivo e ORDER BY e.nombre", EspacioDeportivo.class);
        return query.getResultList();
    }

    /**
     * MÉTODO PRIVADO: guardarInstalacion
     * Persiste una instalación en la base de datos
     * Detecta automáticamente si es creación (INSERT) o actualización (UPDATE)
     */
    private void guardarInstalacion(EspacioDeportivo instalacion) {
        Long id = instalacion.getId();
        try {
            utx.begin();

            if (id == null) {
                // Nueva instalación: usar persist
                em.persist(instalacion);
                LOG.log(Level.INFO, "Nueva instalación guardada: {0}", instalacion.getNombre());
            } else {
                // Instalación existente: usar merge
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
     * MÉTODO PRIVADO: eliminarInstalacion
     * Elimina una instalación de la base de datos
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
}
