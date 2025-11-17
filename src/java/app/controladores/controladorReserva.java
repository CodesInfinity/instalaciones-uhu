package app.controladores;

import app.modelos.Reserva;
import app.modelos.Usuario;
import app.modelos.EspacioDeportivo;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CONTROLADOR DE RESERVAS
 *
 * Gestiona todas las operaciones relacionadas con reservas: - Listado de
 * reservas (admin) - Creación de reservas con integración de Stripe -
 * Visualización de horarios disponibles - Edición y eliminación de reservas -
 * Gestión de pagos mediante Stripe
 *
 * Reglas de negocio: - Cada reserva dura exactamente 1 hora y 30 minutos - No
 * pueden solaparse reservas para el mismo espacio - Profesores no pagan (rol =
 * 2) - Estudiantes pagan según tarifa (con o sin TUO) - No se permiten reservas
 * los fines de semana - Horario permitido: 8:00 - 20:30
 *
 * @author agustinrodriguez
 * @version 1.1
 */
@WebServlet(name = "ControladorReserva", urlPatterns = {"/reservas/*"})
public class controladorReserva extends HttpServlet {

    // ===== INYECCIÓN DE DEPENDENCIAS =====
    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private static final Logger LOG = Logger.getLogger(controladorReserva.class.getName());

    // Clave secreta de Stripe (modo test)
    private static final String STRIPE_SECRET_KEY = System.getenv("STRIPE_SECRET_KEY");
            
    // Duración de las reservas: 1 hora y 30 minutos
    private static final int DURACION_RESERVA_MINUTOS = 90;

    /**
     * MÉTODO: doGet Maneja las peticiones GET del controlador
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/";

        switch (path) {
            // RUTA: /reservas/
            // Listar todas las reservas (solo para admin)
            case "/" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                mostrarReservas(request, response);
            }

            // RUTA: /reservas/panel
            // Panel administrativo de reservas
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarPanelAdmin(request, response);
            }

            // RUTA: /reservas/nueva
            // Formulario para crear nueva reserva
            case "/nueva" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                mostrarFormularioNueva(request, response);
            }

            // RUTA: /reservas/editar
            // Formulario para editar reserva existente (solo admin)
            case "/editar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para editar reservas");
                    return;
                }
                mostrarFormularioEditar(request, response);
            }

            // RUTA: /reservas/borrar
            // Eliminar una reserva
            case "/borrar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para eliminar reservas");
                    return;
                }
                borrarReserva(request, response);
            }

            // RUTA: /reservas/disponibilidad
            // Ver horarios disponibles para una instalación en una fecha
            case "/disponibilidad" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                mostrarDisponibilidad(request, response);
            }

            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    /**
     * MÉTODO: doPost Maneja las peticiones POST del controlador
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            // RUTA: /reservas/crear
            // Procesar creación de reserva (redirige a pago si es necesario)
            case "/crear" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                procesarCrearReserva(request, response);
            }

            // RUTA: /reservas/guardar
            // Guardar edición de reserva (solo admin)
            case "/guardar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos");
                    return;
                }
                procesarGuardarReserva(request, response);
            }

            // RUTA: /reservas/preparar-pago
            // Preparar datos para la vista de pago
            case "/preparar-pago" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                prepararPago(request, response);
            }

            // RUTA: /reservas/procesar-pago
            // Procesar el pago con Stripe y crear la reserva
            case "/procesar-pago" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                procesarPago(request, response);
            }

            default ->
                forwardError(request, response, "Acción no válida");
        }
    }

    /**
     * MÉTODO PRIVADO: mostrarReservas Muestra las reservas del usuario logueado
     */
    private void mostrarReservas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = getUsuarioLogueado(request);
        List<Reserva> reservas;

        if (usuario.getRol() == 0) {
            // Admin: ver todas las reservas
            reservas = obtenerTodasLasReservas();
        } else {
            // Usuario normal: solo sus reservas
            reservas = obtenerReservasDelUsuario(usuario.getId());
        }

        request.setAttribute("reservas", reservas);
        setLayoutAttributes(request, "Mis Reservas",
                "Gestiona tus reservas de instalaciones deportivas");
        request.setAttribute("pageContent", "../reservas/listaReservas.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * MÉTODO PRIVADO: mostrarPanelAdmin Panel administrativo con todas las
     * reservas
     */
    private void mostrarPanelAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Reserva> reservas = obtenerTodasLasReservas();
        request.setAttribute("reservas", reservas);

        setLayoutAttributes(request, "Panel de Reservas",
                "Gestiona todas las reservas del sistema");
        request.setAttribute("pageContent", "../reservas/panelReservas.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * MÉTODO PRIVADO: mostrarFormularioNueva Muestra el formulario para crear
     * una nueva reserva
     */
    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener lista de espacios deportivos disponibles
        List<EspacioDeportivo> espacios = obtenerTodosLosEspacios();
        request.setAttribute("espacios", espacios);

        // Obtener parámetros de la URL para pre-cargar el formulario
        String espacioIdParam = request.getParameter("espacioId");
        String fechaParam = request.getParameter("fecha");
        String horaParam = request.getParameter("hora");

        // Si hay parámetros, pre-cargar los datos
        if (espacioIdParam != null && fechaParam != null && horaParam != null) {
            try {
                Long espacioId = Long.parseLong(espacioIdParam);
                LocalDate fecha = LocalDate.parse(fechaParam);
                LocalTime hora = LocalTime.parse(horaParam);

                // Validar que no sea fin de semana
                if (esFinDeSemana(fecha)) {
                    forwardError(request, response, "No se pueden realizar reservas los fines de semana");
                    return;
                }

                // Validar horario dentro del rango permitido (8:00 - 20:30)
                if (!esHorarioValido(hora)) {
                    forwardError(request, response, "El horario seleccionado no está dentro del rango permitido (8:00 - 20:30)");
                    return;
                }

                request.setAttribute("espacioIdPreseleccionado", espacioId);
                request.setAttribute("fechaPreseleccionada", fecha.toString());
                request.setAttribute("horaPreseleccionada", hora.toString());

                // Calcular hora fin
                LocalTime horaFin = hora.plusMinutes(DURACION_RESERVA_MINUTOS);
                request.setAttribute("horaFinPreseleccionada", horaFin.toString());

            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error al procesar parámetros de pre-carga", e);
            }
        } else {
            request.setAttribute("fechaPreseleccionada", "");
            request.setAttribute("horaPreseleccionada", "08:30");
            request.setAttribute("horaFinPreseleccionada", "10:00");
        }

        setLayoutAttributes(request, "Nueva Reserva",
                "Reserva una instalación deportiva");
        request.setAttribute("pageContent", "../reservas/formReserva.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    /**
     * MÉTODO PRIVADO: mostrarFormularioEditar Muestra el formulario para editar
     * una reserva existente
     */
    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam != null) {
            Reserva reserva = em.find(Reserva.class, Long.parseLong(idParam));
            if (reserva != null) {
                List<EspacioDeportivo> espacios = obtenerTodosLosEspacios();
                request.setAttribute("reserva", reserva);
                request.setAttribute("espacios", espacios);

                setLayoutAttributes(request, "Editar Reserva",
                        "Modifica los datos de la reserva");
                request.setAttribute("pageContent", "../reservas/formReserva.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            } else {
                forwardError(request, response, "Reserva no encontrada.");
            }
        } else {
            forwardError(request, response, "ID de reserva no proporcionado.");
        }
    }

    /**
     * MÉTODO PRIVADO: mostrarDisponibilidad Muestra los horarios disponibles
     * para una instalación en una fecha
     */
    private void mostrarDisponibilidad(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String espacioIdParam = request.getParameter("espacioId");
        String fechaParam = request.getParameter("fecha");

        if (espacioIdParam == null) {
            forwardError(request, response, "ID de instalación no proporcionado");
            return;
        }

        try {
            Long espacioId = Long.parseLong(espacioIdParam);

            LocalDate fecha;
            if (fechaParam == null || fechaParam.trim().isEmpty()) {
                fecha = LocalDate.now();
                // Si hoy es fin de semana, buscar el próximo lunes
                while (esFinDeSemana(fecha)) {
                    fecha = fecha.plusDays(1);
                }
            } else {
                fecha = LocalDate.parse(fechaParam);
            }

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);

            if (espacio == null) {
                forwardError(request, response, "Instalación no encontrada");
                return;
            }

            List<Map<String, Object>> diasDisponibles = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate fechaDia = fecha.plusDays(i);
                Map<String, Object> diaInfo = new HashMap<>();

                diaInfo.put("fechaStr", fechaDia.toString());
                diaInfo.put("activo", fecha.equals(fechaDia));
                diaInfo.put("numero", fechaDia.getDayOfMonth());
                diaInfo.put("esFinDeSemana", esFinDeSemana(fechaDia));

                // Nombre del día
                if (i == 0) {
                    diaInfo.put("nombre", "Hoy");
                } else if (i == 1) {
                    diaInfo.put("nombre", "Mañana");
                } else {
                    String nombreDia = fechaDia.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    diaInfo.put("nombre", nombreDia.substring(0, 3));
                }

                // Mes
                String nombreMes = fechaDia.getMonth()
                        .getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                diaInfo.put("mes", nombreMes);

                diasDisponibles.add(diaInfo);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
            String fechaFormateada = fecha.format(formatter);

            List<String> horariosDisponibles = obtenerHorariosDisponibles(espacioId, fecha);

            List<Map<String, String>> horariosConInfo = new ArrayList<>();
            for (String horario : horariosDisponibles) {
                Map<String, String> horarioInfo = new HashMap<>();
                LocalTime horaInicio = LocalTime.parse(horario);
                LocalTime horaFin = horaInicio.plusMinutes(90);

                horarioInfo.put("inicio", horario);
                horarioInfo.put("fin", horaFin.toString());
                horarioInfo.put("fecha", fecha.toString());

                horariosConInfo.add(horarioInfo);
            }

            request.setAttribute("espacio", espacio);
            request.setAttribute("fecha", fecha.toString());
            request.setAttribute("fechaFormateada", fechaFormateada);
            request.setAttribute("diasDisponibles", diasDisponibles);
            request.setAttribute("horariosConInfo", horariosConInfo);

            setLayoutAttributes(request, "Horarios Disponibles",
                    "Selecciona un horario para tu reserva");
            request.setAttribute("pageContent", "../reservas/disponibilidadInstalacion.jsp");
            forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[v0] Error al obtener disponibilidad: " + e.getMessage(), e);
            forwardError(request, response, "Error al obtener disponibilidad: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: procesarCrearReserva Procesa la creación de una nueva
     * reserva (redirige a pago si es necesario)
     */
    private void procesarCrearReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Usuario usuario = getUsuarioLogueado(request);
            String espacioIdParam = request.getParameter("espacioId");
            String fechaParam = request.getParameter("fecha");
            String horaParam = request.getParameter("hora");
            String tieneTuoParam = request.getParameter("tieneTuo");

            // Validar parámetros
            if (espacioIdParam == null || fechaParam == null || horaParam == null) {
                forwardError(request, response, "Todos los campos son obligatorios");
                return;
            }

            Long espacioId = Long.parseLong(espacioIdParam);
            LocalDate fecha = LocalDate.parse(fechaParam);
            LocalTime hora = LocalTime.parse(horaParam);
            boolean tieneTuo = "true".equals(tieneTuoParam);

            // VALIDACIÓN: No permitir fines de semana
            if (esFinDeSemana(fecha)) {
                forwardError(request, response, "No se pueden realizar reservas los fines de semana");
                return;
            }

            // VALIDACIÓN: Horario dentro del rango permitido
            if (!esHorarioValido(hora)) {
                forwardError(request, response, "El horario seleccionado no está dentro del rango permitido (8:00 - 20:30)");
                return;
            }

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            if (espacio == null) {
                forwardError(request, response, "Instalación no encontrada");
                return;
            }

            // Crear fecha y hora de inicio y fin
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            // Verificar que el usuario no tenga otra reserva en el mismo horario
            if (usuarioTieneReservaEnHorario(usuario.getId(), inicio, fin, null)) {
                forwardError(request, response, "Ya tiene una reserva activa en ese horario.");
                return;
            }

            // Verificar que no haya colisiones
            if (existeColision(espacioId, inicio, fin, null)) {
                forwardError(request, response, "El horario seleccionado ya está reservado");
                return;
            }

            // Calcular precio
            BigDecimal precio = calcularPrecio(espacio, usuario, tieneTuo);

            // Si el usuario es profesor (rol = 2) o el precio es 0, crear reserva directamente
            if (usuario.getRol() == 2 || precio.compareTo(BigDecimal.ZERO) == 0) {
                // Crear reserva directamente sin pago
                Reserva reserva = new Reserva(usuario, espacio, inicio, fin);
                guardarReserva(reserva);
                response.sendRedirect(request.getContextPath() + "/reservas/?success");
                return;
            }

            // Para estudiantes que deben pagar, guardar datos en la REQUEST en lugar de sesión
            request.setAttribute("reservaTemporal_espacioId", espacioId.toString());
            request.setAttribute("reservaTemporal_fecha", fecha.toString());
            request.setAttribute("reservaTemporal_hora", hora.toString());
            request.setAttribute("reservaTemporal_tieneTuo", String.valueOf(tieneTuo));
            request.setAttribute("reservaTemporal_precio", precio.toString());
            request.setAttribute("reservaTemporal_inicio", inicio.toString());
            request.setAttribute("reservaTemporal_fin", fin.toString());
            request.setAttribute("reservaTemporal_espacio", espacio);

            LOG.log(Level.INFO, "[v4] Datos de reserva guardados en request para pago - Precio: {0}, Espacio: {1}",
                    new Object[]{precio, espacio.getNombre()});

            // Usar FORWARD en lugar de REDIRECT para mantener los datos
            prepararPago(request, response);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al procesar reserva", e);
            forwardError(request, response, "Error al procesar la reserva: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: procesarGuardarReserva Procesa la edición de una reserva
     * (solo admin)
     */
    private void procesarGuardarReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String idParam = request.getParameter("id");
            String espacioIdParam = request.getParameter("espacioId");
            String fechaParam = request.getParameter("fecha");
            String horaParam = request.getParameter("hora");

            if (espacioIdParam == null || fechaParam == null || horaParam == null) {
                forwardError(request, response, "Todos los campos son obligatorios");
                return;
            }

            Long espacioId = Long.parseLong(espacioIdParam);
            LocalDate fecha = LocalDate.parse(fechaParam);
            LocalTime hora = LocalTime.parse(horaParam);

            // VALIDACIÓN: No permitir fines de semana
            if (esFinDeSemana(fecha)) {
                forwardError(request, response, "No se pueden realizar reservas los fines de semana");
                return;
            }

            // VALIDACIÓN: Horario dentro del rango permitido
            if (!esHorarioValido(hora)) {
                forwardError(request, response, "El horario seleccionado no está dentro del rango permitido (8:00 - 20:30)");
                return;
            }

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);

            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            if (idParam != null && !idParam.trim().isEmpty()) {
                // EDITAR reserva existente
                Long id = Long.parseLong(idParam);
                Reserva reserva = em.find(Reserva.class, id);

                if (reserva == null) {
                    forwardError(request, response, "Reserva no encontrada");
                    return;
                }

                if (usuarioTieneReservaEnHorario(reserva.getUsuario().getId(), inicio, fin, reserva.getId())) {
                    forwardError(request, response, "Ya tiene otra reserva en ese horario.");
                    return;
                }

                // Verificar colisiones (excluyendo esta reserva)
                if (existeColision(espacioId, inicio, fin, id)) {
                    forwardError(request, response, "El horario seleccionado ya está reservado");
                    return;
                }

                reserva.setEspacio(espacio);
                reserva.setInicio(inicio);
                reserva.setFin(fin);

                guardarReserva(reserva);
            }

            response.sendRedirect(request.getContextPath() + "/reservas/panel");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar reserva", e);
            forwardError(request, response, "Error al guardar la reserva: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: borrarReserva Elimina una reserva del sistema
     */
    private void borrarReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam != null) {
            try {
                eliminarReserva(Long.parseLong(idParam));
                response.sendRedirect(request.getContextPath() + "/reservas/panel");
            } catch (Exception e) {
                forwardError(request, response, "Error al eliminar la reserva: " + e.getMessage());
            }
        } else {
            forwardError(request, response, "ID de reserva no proporcionado.");
        }
    }

    /**
     * MÉTODO PRIVADO: prepararPago Prepara los datos para la vista de pago
     * usando la REQUEST
     */
    private void prepararPago(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Obtener datos de la REQUEST en lugar de sesión
            String espacioIdStr = (String) request.getAttribute("reservaTemporal_espacioId");
            String fechaStr = (String) request.getAttribute("reservaTemporal_fecha");
            String horaStr = (String) request.getAttribute("reservaTemporal_hora");
            String tieneTuoStr = (String) request.getAttribute("reservaTemporal_tieneTuo");
            String precioStr = (String) request.getAttribute("reservaTemporal_precio");
            String inicioStr = (String) request.getAttribute("reservaTemporal_inicio");
            String finStr = (String) request.getAttribute("reservaTemporal_fin");
            EspacioDeportivo espacio = (EspacioDeportivo) request.getAttribute("reservaTemporal_espacio");

            LOG.log(Level.INFO, "[v4] Recuperando datos de request - espacioId: {0}, fecha: {1}",
                    new Object[]{espacioIdStr, fechaStr});

            // Validar que todos los datos estén presentes
            if (espacioIdStr == null || fechaStr == null || horaStr == null || precioStr == null || espacio == null) {
                LOG.log(Level.SEVERE, "[v4] Datos de reserva temporal incompletos en request");
                forwardError(request, response, "Error: Datos de reserva no encontrados. Por favor, comienza de nuevo.");
                return;
            }

            // Convertir datos
            Long espacioId = Long.parseLong(espacioIdStr);
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            boolean tieneTuo = Boolean.parseBoolean(tieneTuoStr);
            LocalDateTime inicio = LocalDateTime.parse(inicioStr);
            LocalDateTime fin = LocalDateTime.parse(finStr);

            // Verificar nuevamente que no haya colisiones
            if (existeColision(espacioId, inicio, fin, null)) {
                LOG.log(Level.WARNING, "[v4] El horario ya está reservado al verificar nuevamente");
                forwardError(request, response, "El horario seleccionado ya está reservado. Por favor, selecciona otro horario.");
                return;
            }

            // Preparar datos para la vista de pago
            request.setAttribute("espacio", espacio);
            request.setAttribute("espacioId", espacioId);
            request.setAttribute("fecha", fechaStr);
            request.setAttribute("hora", horaStr);
            request.setAttribute("horaInicio", hora.format(DateTimeFormatter.ofPattern("HH:mm")));
            request.setAttribute("horaFin", fin.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            request.setAttribute("tieneTuo", tieneTuo);
            request.setAttribute("precio", precioStr);

            LOG.log(Level.INFO, "[v4] Preparando vista de pago - Espacio: {0}, Precio: {1}",
                    new Object[]{espacio.getNombre(), precioStr});

            setLayoutAttributes(request, "Pago de Reserva", "Completa tu pago de forma segura");
            request.setAttribute("pageContent", "../reservas/pagoReserva.jsp");
            forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[v4] Error al preparar pago: " + e.getMessage(), e);
            forwardError(request, response, "Error al preparar el pago: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: procesarPago Procesa el pago con Stripe y crea la reserva
     */
    private void procesarPago(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            LOG.log(Level.INFO, "[v4] Iniciando procesamiento de pago");

            Usuario usuario = getUsuarioLogueado(request);

            // Obtener datos del formulario de pago
            String espacioIdStr = request.getParameter("espacioId");
            String fechaStr = request.getParameter("fecha");
            String horaStr = request.getParameter("hora");
            String tieneTuoStr = request.getParameter("tieneTuo");
            String precioStr = request.getParameter("precio");
            String paymentMethodId = request.getParameter("paymentMethodId");

            LOG.log(Level.INFO, "[v4] Parámetros recibidos - espacioId: {0}, fecha: {1}, hora: {2}, precio: {3}",
                    new Object[]{espacioIdStr, fechaStr, horaStr, precioStr});
            LOG.log(Level.INFO, "[v4] paymentMethodId recibido: {0}", paymentMethodId);

            // Validar que todos los datos estén presentes
            if (espacioIdStr == null || fechaStr == null || horaStr == null || precioStr == null) {
                LOG.log(Level.SEVERE, "[v4] Error: Parámetros incompletos");
                forwardError(request, response, "Error: Datos de reserva incompletos. Por favor, comienza de nuevo.");
                return;
            }

            // Validar paymentMethodId
            if (paymentMethodId == null || paymentMethodId.trim().isEmpty() || "null".equals(paymentMethodId)) {
                LOG.log(Level.SEVERE, "[v4] Error: paymentMethodId inválido: {0}", paymentMethodId);
                forwardError(request, response, "Error: No se recibió un método de pago válido. Por favor, intenta nuevamente.");
                return;
            }

            // Convertir datos
            Long espacioId = Long.parseLong(espacioIdStr);
            BigDecimal precio = new BigDecimal(precioStr);
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            boolean tieneTuo = Boolean.parseBoolean(tieneTuoStr);

            // Reconstruir fechas
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            if (espacio == null) {
                LOG.log(Level.SEVERE, "[v4] Error: Espacio no encontrado con ID: {0}", espacioId);
                forwardError(request, response, "Instalación no encontrada");
                return;
            }

            LOG.log(Level.INFO, "[v4] Datos validados - Monto: {0} EUR, Espacio: {1}",
                    new Object[]{precio, espacio.getNombre()});

            // Verificar que no haya colisiones nuevamente
            if (existeColision(espacioId, inicio, fin, null)) {
                LOG.log(Level.WARNING, "[v4] El horario ya está reservado");
                forwardError(request, response, "El horario seleccionado ya está reservado. Por favor, selecciona otro horario.");
                return;
            }

            LOG.log(Level.INFO, "[v4] No hay colisiones, procediendo con el pago a Stripe");

            // Procesar pago con Stripe
            boolean pagoExitoso = procesarPagoStripe(precio, usuario, espacio, paymentMethodId);

            LOG.log(Level.INFO, "[v4] Resultado del pago Stripe: {0}", pagoExitoso ? "EXITOSO" : "FALLIDO");

            if (pagoExitoso) {
                // Crear reserva
                Reserva reserva = new Reserva(usuario, espacio, inicio, fin);
                guardarReserva(reserva);

                LOG.log(Level.INFO, "[v4] Reserva creada exitosamente");
                response.sendRedirect(request.getContextPath() + "/reservas/?success=Reserva creada y pago procesado correctamente");
            } else {
                LOG.log(Level.SEVERE, "[v4] El pago con Stripe falló");
                forwardError(request, response, "Error en el pago. La reserva no se ha realizado. Por favor, intenta con otra tarjeta.");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[v4] Excepción al procesar pago: " + e.getMessage(), e);
            forwardError(request, response, "Error al procesar el pago: " + e.getMessage());
        }
    }

    // ===== MÉTODOS DE ACCESO A DATOS =====
    /**
     * MÉTODO PRIVADO: obtenerTodasLasReservas Obtiene todas las reservas del
     * sistema ordenadas por fecha
     */
    private List<Reserva> obtenerTodasLasReservas() {
        TypedQuery<Reserva> query = em.createQuery(
                "SELECT r FROM Reserva r ORDER BY r.inicio DESC", Reserva.class);
        return query.getResultList();
    }

    /**
     * MÉTODO PRIVADO: obtenerReservasDelUsuario Obtiene las reservas de un
     * usuario específico
     */
    private List<Reserva> obtenerReservasDelUsuario(Long usuarioId) {
        TypedQuery<Reserva> query = em.createQuery(
                "SELECT r FROM Reserva r WHERE r.usuario.id = :usuarioId ORDER BY r.inicio DESC",
                Reserva.class);
        query.setParameter("usuarioId", usuarioId);
        return query.getResultList();
    }

    /**
     * MÉTODO PRIVADO: obtenerTodosLosEspacios Obtiene todos los espacios
     * deportivos
     */
    private List<EspacioDeportivo> obtenerTodosLosEspacios() {
        TypedQuery<EspacioDeportivo> query = em.createQuery(
                "SELECT e FROM EspacioDeportivo e ORDER BY e.nombre", EspacioDeportivo.class);
        return query.getResultList();
    }

    /**
     * MÉTODO PRIVADO: obtenerHorariosDisponibles Genera una lista de horarios
     * disponibles para una instalación en una fecha
     */
    private List<String> obtenerHorariosDisponibles(Long espacioId, LocalDate fecha) {
        List<String> horarios = new ArrayList<>();

        // Verificar si es fin de semana
        if (esFinDeSemana(fecha)) {
            return horarios; // Lista vacía para fines de semana
        }

        // Horario de apertura: 8:30 - 20:30 
        LocalTime horaInicio = LocalTime.of(8, 30);
        LocalTime horaFin = LocalTime.of(20, 30);

        // Generar horarios cada 1 hora y 30 minutos
        LocalTime horaActual = horaInicio;
        while (horaActual.plusMinutes(DURACION_RESERVA_MINUTOS).isBefore(horaFin.plusMinutes(1))) {
            LocalDateTime inicio = LocalDateTime.of(fecha, horaActual);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            // Verificar si está disponible
            if (!existeColision(espacioId, inicio, fin, null)) {
                horarios.add(horaActual.toString());
            }

            horaActual = horaActual.plusMinutes(DURACION_RESERVA_MINUTOS);
        }

        return horarios;
    }

    /**
     * MÉTODO PRIVADO: existeColision Verifica si existe una reserva que se
     * solape con el horario dado
     */
    private boolean existeColision(Long espacioId, LocalDateTime inicio, LocalDateTime fin, Long excludeId) {
        String jpql = "SELECT COUNT(r) FROM Reserva r WHERE r.espacio.id = :espacioId "
                + "AND ((r.inicio < :fin AND r.fin > :inicio))";

        if (excludeId != null) {
            jpql += " AND r.id != :excludeId";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("espacioId", espacioId);
        query.setParameter("inicio", inicio);
        query.setParameter("fin", fin);

        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        return query.getSingleResult() > 0;
    }

    /**
     * MÉTODO PRIVADO: usuarioTieneReservaEnHorario Verifica si existe una
     * reserva existente el mismo dia/hora.
     *
     * @param usuarioId
     * @param inicio
     * @param fin
     * @param excludeId
     * @return
     */
    private boolean usuarioTieneReservaEnHorario(Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin,
            Long excludeId) {
        String jpql = "SELECT COUNT(r) FROM Reserva r WHERE r.usuario.id = :usuarioId "
                + "AND (r.inicio < :fin AND r.fin > :inicio)";

        if (excludeId != null) {
            jpql += " AND r.id != :excludeId";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("usuarioId", usuarioId);
        query.setParameter("inicio", inicio);
        query.setParameter("fin", fin);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    /**
     * MÉTODO PRIVADO: calcularPrecio Calcula el precio de la reserva según el
     * tipo de instalación y si tiene TUO
     */
    private BigDecimal calcularPrecio(EspacioDeportivo espacio, Usuario usuario, boolean tieneTuo) {
        // Si es profesor, precio 0
        if (usuario.getRol() == 2) {
            return BigDecimal.ZERO;
        }

        String tipo = espacio.getTipo().toLowerCase();
        String nombre = espacio.getNombre().toLowerCase();
        String descripcion = espacio.getDescripcion() != null ? espacio.getDescripcion().toLowerCase() : "";

        // Pabellón Cubierto (1 Y 2)
        if (nombre.contains("pabellón") || nombre.contains("pabellon")
                || tipo.contains("pabellón") || tipo.contains("pabellon")) {
            boolean conLuz = nombre.contains("luz") || descripcion.contains("luz");

            if (tieneTuo) {
                return conLuz ? new BigDecimal("12.00") : new BigDecimal("9.00");
            } else {
                return conLuz ? new BigDecimal("30.00") : new BigDecimal("25.00");
            }
        }

        // Aula
        if (tipo.contains("aula") || nombre.contains("aula")) {
            return tieneTuo ? BigDecimal.ZERO : new BigDecimal("15.00");
        }

        // Sala Usos Múltiples
        if (tipo.contains("sala") || nombre.contains("sala")) {
            return tieneTuo ? BigDecimal.ZERO : new BigDecimal("15.00");
        }

        // Pistas Tenis
        if (tipo.contains("tenis") || nombre.contains("tenis")) {
            boolean conLuz = nombre.contains("luz") || descripcion.contains("luz");
            return tieneTuo
                    ? (conLuz ? new BigDecimal("2.00") : new BigDecimal("1.50"))
                    : (conLuz ? new BigDecimal("6.00") : new BigDecimal("4.00"));
        }

        // Pistas Pádel
        if (tipo.contains("pádel") || tipo.contains("padel")
                || nombre.contains("pádel") || nombre.contains("padel")) {
            boolean conLuz = nombre.contains("luz") || descripcion.contains("luz");
            return tieneTuo
                    ? (conLuz ? new BigDecimal("2.00") : new BigDecimal("1.50"))
                    : (conLuz ? new BigDecimal("6.00") : new BigDecimal("4.00"));
        }

        return BigDecimal.ZERO;
    }

    /**
     * MÉTODO PRIVADO: procesarPagoStripe Procesa un pago mediante la API de
     * Stripe usando Jakarta JSON
     */
    private boolean procesarPagoStripe(BigDecimal precio, Usuario usuario, EspacioDeportivo espacio, String paymentMethodId) {
        try {
            // Validar paymentMethodId de manera más estricta
            if (paymentMethodId == null || paymentMethodId.trim().isEmpty() || "null".equals(paymentMethodId)) {
                LOG.log(Level.SEVERE, "[v2] Error: paymentMethodId inválido en procesarPagoStripe: {0}", paymentMethodId);
                return false;
            }

            // Convertir precio a céntimos
            int amountInCents = precio.multiply(new BigDecimal("100")).intValue();
            LOG.log(Level.INFO, "[v2] Paso en céntimos: {0}", amountInCents);
            LOG.log(Level.INFO, "[v2] PaymentMethodId recibido: {0}", paymentMethodId);

            configurarSSLParaDesarrollo();

            // Crear Payment Intent en Stripe
            URL url = new URL("https://api.stripe.com/v1/payment_intents");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + STRIPE_SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Construir datos del formulario
            String postData = "amount=" + amountInCents
                    + "&currency=eur"
                    + "&payment_method=" + URLEncoder.encode(paymentMethodId, "UTF-8")
                    + "&confirm=true"
                    + "&automatic_payment_methods[enabled]=true"
                    + "&automatic_payment_methods[allow_redirects]=never"
                    + "&description=Reserva: " + URLEncoder.encode(espacio.getNombre(), "UTF-8")
                    + "&receipt_email=" + URLEncoder.encode(usuario.getEmail() != null ? usuario.getEmail() : "", "UTF-8");

            LOG.log(Level.INFO, "[v2] Enviando petición a Stripe API");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            LOG.log(Level.INFO, "[v2] Código de respuesta de Stripe: {0}", responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                LOG.log(Level.INFO, "[v2] Respuesta exitosa de Stripe: {0}", response.toString());

                // Parsear respuesta JSON usando Jakarta JSON
                try (JsonReader jsonReader = Json.createReader(new StringReader(response.toString()))) {
                    JsonObject jsonResponse = jsonReader.readObject();
                    String status = jsonResponse.getString("status", "unknown");

                    LOG.log(Level.INFO, "[v2] Pago Stripe procesado con status: {0}", status);

                    // Considerar el pago exitoso si está succeeded o requires_capture
                    boolean exitoso = "succeeded".equals(status) || "requires_capture".equals(status);
                    LOG.log(Level.INFO, "[v2] ¿Pago exitoso?: {0}", exitoso);

                    return exitoso;
                }
            } else {
                // Manejo de errores mejorado
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String inputLine;
                StringBuilder errorResponse = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                in.close();

                LOG.log(Level.SEVERE, "[v2] Error en Stripe. Código: {0}. Respuesta: {1}",
                        new Object[]{responseCode, errorResponse.toString()});

                // Intentar parsear el error para obtener más detalles
                try (JsonReader jsonReader = Json.createReader(new StringReader(errorResponse.toString()))) {
                    JsonObject errorJson = jsonReader.readObject();
                    if (errorJson.containsKey("error")) {
                        JsonObject error = errorJson.getJsonObject("error");
                        String errorMessage = error.getString("message", "Error desconocido");
                        String errorType = error.getString("type", "unknown");
                        LOG.log(Level.SEVERE, "[v2] Stripe error type: {0}, message: {1}",
                                new Object[]{errorType, errorMessage});
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "[v2] No se pudo parsear el error de Stripe como JSON");
                }

                return false;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[v2] Excepción al procesar pago con Stripe: " + e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * MÉTODO PRIVADO: guardarReserva Persiste una reserva en la base de datos
     */
    private void guardarReserva(Reserva reserva) {
        Long id = reserva.getId();
        try {
            utx.begin();

            if (id == null) {
                em.persist(reserva);
                LOG.log(Level.INFO, "Nueva reserva guardada");
            } else {
                em.merge(reserva);
                LOG.log(Level.INFO, "Reserva {0} actualizada", id);
            }

            utx.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Excepción al guardar reserva", e);
            try {
                utx.rollback();
            } catch (Exception rollbackEx) {
                LOG.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * MÉTODO PRIVADO: eliminarReserva Elimina una reserva de la base de datos
     */
    private void eliminarReserva(Long id) {
        try {
            utx.begin();
            Reserva reserva = em.find(Reserva.class, id);
            if (reserva != null) {
                em.remove(reserva);
                LOG.log(Level.INFO, "Reserva eliminada: {0}", id);
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
     * MÉTODO PRIVADO: estaLogueado Verifica si hay un usuario en la sesión
     */
    private boolean estaLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return usuario != null;
        }
        return false;
    }

    /**
     * MÉTODO PRIVADO: esAdministrador Verifica si el usuario logueado es
     * administrador
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
     * MÉTODO PRIVADO: getUsuarioLogueado Obtiene el usuario de la sesión
     */
    private Usuario getUsuarioLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Usuario) session.getAttribute("usuario");
        }
        return null;
    }

    /**
     * MÉTODO PRIVADO: setLayoutAttributes Establece los atributos para el
     * layout
     */
    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    /**
     * MÉTODO PRIVADO: forward Realiza un forward a una JSP
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher(vista);
        rd.forward(request, response);
    }

    /**
     * MÉTODO PRIVADO: forwardError Muestra la página de error
     */
    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }

    /**
     * MÉTODO PRIVADO: configurarSSLParaDesarrollo Configura SSL para aceptar
     * todos los certificados (SOLO PARA DESARROLLO) ADVERTENCIA: No usar en
     * producción
     */
    private void configurarSSLParaDesarrollo() {
        try {
            // Crear un TrustManager que acepta todos los certificados
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Instalar el TrustManager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Crear un verificador de hostname que acepta todos los hosts
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            LOG.log(Level.INFO, "[v2] Configuración SSL para desarrollo aplicada");

        } catch (Exception e) {
            LOG.log(Level.WARNING, "[v2] No se pudo configurar SSL: " + e.getMessage());
        }
    }

    /**
     * MÉTODO PRIVADO: limpiarSesionReserva Limpia los datos temporales de
     * reserva de la sesión
     */
    private void limpiarSesionReserva(HttpSession session) {
        if (session != null) {
            session.removeAttribute("reservaTemporal_espacioId");
            session.removeAttribute("reservaTemporal_fecha");
            session.removeAttribute("reservaTemporal_hora");
            session.removeAttribute("reservaTemporal_tieneTuo");
            session.removeAttribute("reservaTemporal_precio");
            session.removeAttribute("reservaTemporal_inicio");
            session.removeAttribute("reservaTemporal_fin");
            session.removeAttribute("reservaTemporal_usuarioId");

            LOG.log(Level.INFO, "[v3] Datos temporales de reserva limpiados de la sesión");
        }
    }

    /**
     * MÉTODO PRIVADO: esFinDeSemana Verifica si una fecha es sábado o domingo
     */
    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    /**
     * MÉTODO PRIVADO: esHorarioValido Verifica si el horario está dentro del
     * rango permitido (8:30 - 20:30)
     */
    private boolean esHorarioValido(LocalTime hora) {
        LocalTime horaMinima = LocalTime.of(8, 30);
        LocalTime horaMaxima = LocalTime.of(20, 30);
        return !hora.isBefore(horaMinima) && !hora.isAfter(horaMaxima);
    }
}
