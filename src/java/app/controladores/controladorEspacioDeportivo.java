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
import jakarta.servlet.annotation.MultipartConfig; // <-- IMPORTANTE
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part; // <-- IMPORTANTE
import jakarta.transaction.UserTransaction;
import java.io.File; // <-- IMPORTANTE
import java.util.List;
import java.util.UUID; // <-- IMPORTANTE
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 *
 * @author agustinrodriguez
 */
@MultipartConfig // <-- IMPORTANTE: Habilita la subida de archivos
@WebServlet(name = "ControladorEspacioDeportivo", urlPatterns = {"/instalaciones/*"})
public class controladorEspacioDeportivo extends HttpServlet {

    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;
    @Resource
    private UserTransaction utx;
    private static final Logger Log = Logger.getLogger(controladorEspacioDeportivo.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/";

        switch (path) {
            case "/" -> {
                mostrarInstalaciones(request, response);
            }
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarPanelAdmin(request, response);
            }
            case "/nueva" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para crear instalaciones");
                    return;
                }
                mostrarFormularioNueva(request, response);
            }
            case "/editar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para editar instalaciones");
                    return;
                }
                mostrarFormularioEditar(request, response);
            }
            case "/detalle" -> {
                mostrarDetalle(request, response);
            }
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

    // MÉTODOS PRINCIPALES
    private void mostrarInstalaciones(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<EspacioDeportivo> instalaciones = obtenerTodasLasInstalaciones();
        request.setAttribute("instalaciones", instalaciones);
        
        setLayoutAttributes(request, "Instalaciones Deportivas", 
                "Descubre todos nuestros espacios deportivos disponibles");
        request.setAttribute("pageContent", "../instalaciones/listaInstalaciones.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void mostrarPanelAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<EspacioDeportivo> instalaciones = obtenerTodasLasInstalaciones();
        request.setAttribute("instalaciones", instalaciones);
        
        setLayoutAttributes(request, "Panel de Instalaciones", 
                "Gestiona todas las instalaciones deportivas del sistema");
        request.setAttribute("pageContent", "../instalaciones/panelInstalaciones.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setLayoutAttributes(request, "Nueva Instalación", 
                "Agrega una nueva instalación deportiva al sistema");
        request.setAttribute("pageContent", "../instalaciones/formInstalacion.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

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

private void procesarGuardarInstalacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // --- 1. Obtener parámetros de texto ---
        String idParam = request.getParameter("id");
        String nombre = request.getParameter("nombre");
        String tipo = request.getParameter("tipo");
        String ubicacion = request.getParameter("ubicacion");
        String descripcion = request.getParameter("descripcion");
        String imagenUrlActual = request.getParameter("imagenUrlActual"); 

        // --- 2. Lógica de subida de archivo (VERSIÓN "DOBLE GUARDADO") ---
        String urlParaDB = imagenUrlActual; 
        Part filePart = request.getPart("imagen");
        String fileName = filePart.getSubmittedFileName();

        if (fileName != null && !fileName.isEmpty()) {
            try {
                // --- Crear un nombre de archivo único ---
                String extension = fileName.substring(fileName.lastIndexOf("."));
                String uniqueName = UUID.randomUUID().toString() + extension;
                
                // --- Definir la URL para la base de datos ---
                urlParaDB = "/img/instalaciones/temp/" + uniqueName;

                // --- Definir Rutas ---
                String appPath = getServletContext().getRealPath("/"); // Ruta de despliegue (build/web)
                String sourcePath = appPath.replace("build" + File.separator + "web", "web"); // Ruta de proyecto (web)

                // --- Ruta 1: Carpeta de Despliegue (build/web/img/...) ---
                Path deployPath = Paths.get(appPath + File.separator + 
                                            "img" + File.separator + 
                                            "instalaciones" + File.separator + 
                                            "temp");

                // --- Ruta 2: Carpeta de Proyecto (web/img/...) ---
                Path projectPath = Paths.get(sourcePath + File.separator + 
                                             "img" + File.separator + 
                                             "instalaciones" + File.separator + 
                                             "temp");

                // --- Crear directorios (si no existen) ---
                if (!Files.exists(deployPath)) Files.createDirectories(deployPath);
                if (!Files.exists(projectPath)) Files.createDirectories(projectPath);

                // --- Definir los archivos de destino ---
                Path deployFile = deployPath.resolve(uniqueName);
                Path projectFile = projectPath.resolve(uniqueName);

                // --- PASO A: Guardar en la carpeta de despliegue (RÁPIDO) ---
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, deployFile, StandardCopyOption.REPLACE_EXISTING);
                }
                Log.log(Level.INFO, "Archivo guardado en despliegue: {0}", deployFile);

                // --- PASO B: Copiar la foto a la carpeta de tu proyecto (LENTO) ---
                // Hacemos esto DESPUÉS para que no bloquee la redirección
                Files.copy(deployFile, projectFile, StandardCopyOption.REPLACE_EXISTING);
                Log.log(Level.INFO, "Archivo copiado a proyecto: {0}", projectFile);


            } catch (Exception e) {
                Log.log(Level.SEVERE, "Error al subir el archivo (doble guardado)", e);
                e.printStackTrace(); 
                forwardError(request, response, "Error al guardar la imagen: " + e.getMessage());
                return;
            }
        }

        // --- 3. Lógica de guardado en BBDD (Sin cambios) ---
        try {
            // (El resto del método sigue exactamente igual)
            // Validar campos requeridos
            if (nombre == null || tipo == null || ubicacion == null ||
                nombre.trim().isEmpty() || tipo.trim().isEmpty() || ubicacion.trim().isEmpty()) {
                forwardError(request, response, "Nombre, tipo y ubicación son campos obligatorios");
                return;
            }

            // Normalizar datos
            String nombreNormalizado = nombre.trim();
            String tipoNormalizado = tipo.trim();
            String ubicacionNormalizada = ubicacion.trim();
            String descripcionNormalizada = descripcion != null ? descripcion.trim() : "";
            String imagenUrlNormalizada = (urlParaDB != null && !urlParaDB.isEmpty()) ? urlParaDB.trim() : null;

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
                instalacion.setImagenUrl(imagenUrlNormalizada);

            } else {
                // MODO NUEVA INSTALACIÓN
                instalacion = new EspacioDeportivo(
                    nombreNormalizado,
                    tipoNormalizado,
                    ubicacionNormalizada,
                    descripcionNormalizada,
                    imagenUrlNormalizada
                );
            }

            // Guardar en base de datos
            guardarInstalacion(instalacion);

            // Redirigir al panel de administración
            response.sendRedirect(request.getContextPath() + "/instalaciones/panel");

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Error al guardar instalación en BBDD", e);
            forwardError(request, response, "Error inesperado al guardar la instalación: " + e.getMessage());
        }
    }
    
    private void borrarInstalacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                // OPCIONAL: Aquí se podría añadir lógica para borrar el archivo de imagen del servidor
                
                eliminarInstalacion(Long.parseLong(idParam));
                response.sendRedirect(request.getContextPath() + "/instalaciones/panel");
            } catch (Exception e) {
                forwardError(request, response, "Error al eliminar la instalación: " + e.getMessage());
            }
        } else {
            forwardError(request, response, "ID de instalación no proporcionado.");
        }
    }

    // MÉTODOS DE ACCESO A DATOS
    private List<EspacioDeportivo> obtenerTodasLasInstalaciones() {
        TypedQuery<EspacioDeportivo> query = em.createQuery(
            "SELECT e FROM EspacioDeportivo e ORDER BY e.nombre", EspacioDeportivo.class);
        return query.getResultList();
    }

    private void guardarInstalacion(EspacioDeportivo instalacion) {
        Long id = instalacion.getId();
        try {
            utx.begin();

            if (id == null) {
                em.persist(instalacion);
                Log.log(Level.INFO, "Nueva instalación guardada: {0}", instalacion.getNombre());
            } else {
                em.merge(instalacion);
                Log.log(Level.INFO, "Instalación actualizada: {0}", instalacion.getNombre());
            }

            utx.commit();

        } catch (Exception e) {
            Log.log(Level.SEVERE, "Excepción al guardar instalación", e);
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                Log.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
            }
            throw new RuntimeException(e);
        }
    }

    private void eliminarInstalacion(Long id) {
        try {
            utx.begin();
            EspacioDeportivo instalacion = em.find(EspacioDeportivo.class, id);
            if (instalacion != null) {
                em.remove(instalacion);
                Log.log(Level.INFO, "Instalación eliminada: {0}", instalacion.getNombre());
            }
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception ex) {
                Log.log(Level.SEVERE, "Error al hacer rollback", ex);
            }
            throw new RuntimeException(e);
        }
    }

    // MÉTODOS AUXILIARES
    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return usuario != null && usuario.getRol() == 0;
        }
        return false;
    }

    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
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
}