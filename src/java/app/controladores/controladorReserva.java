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
 * CONTROLADOR DE GESTIÓN DE RESERVAS
 * * <p>Este Servlet gestiona el ciclo de vida completo de las reservas de instalaciones deportivas.</p>
 * * <p><strong>Características principales:</strong></p>
 * <ul>
 * <li>Gestión de calendario y disponibilidad (bloques de 90 minutos).</li>
 * <li>Cálculo dinámico de precios basado en tipo de usuario (TUO) e instalación.</li>
 * <li>Integración con pasarela de pagos <strong>Stripe</strong>.</li>
 * <li>Prevención de colisiones de horario (Double booking).</li>
 * <li>Endpoints AJAX para actualización dinámica del frontend.</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 3.0 - Integración con Stripe API nativa
 */
@WebServlet(name = "ControladorReserva", urlPatterns = {"/reservas/*"})
public class controladorReserva extends HttpServlet {

    // ==========================================
    // INYECCIÓN DE DEPENDENCIAS
    // ==========================================

    @PersistenceContext(unitName = "instalacionesPU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    private static final Logger LOG = Logger.getLogger(controladorReserva.class.getName());
    
    // Clave secreta de Stripe (Test Mode). En producción debería estar en variables de entorno.
    private static final String STRIPE_SECRET_KEY = "sk_";
    
    // Duración estándar de cada bloque de reserva
    private static final int DURACION_RESERVA_MINUTOS = 90;

    // ==========================================
    // ENRUTAMIENTO (GET)
    // ==========================================

    /**
     * Maneja la navegación y visualización de datos.
     * Soporta tanto renderizado de vistas JSP como respuestas JSON para AJAX.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo() != null ? request.getPathInfo() : "/";
        String ajax = request.getParameter("ajax");
        boolean esAjax = "true".equals(ajax);
        
        LOG.log(Level.INFO, "[v1] doGet - path: {0}, esAjax: {1}", new Object[]{path, esAjax});

        switch (path) {
            // RUTA: /reservas/ - Listado personal del usuario
            case "/" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                mostrarReservas(request, response);
            }

            // RUTA: /reservas/panel - Gestión global (Admin)
            case "/panel" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para acceder a esta sección");
                    return;
                }
                mostrarPanelAdmin(request, response);
            }

            // RUTA: /reservas/nueva - Formulario de creación
            case "/nueva" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                mostrarFormularioNueva(request, response);
            }

            // RUTA: /reservas/editar - Edición (Admin)
            case "/editar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para editar reservas");
                    return;
                }
                mostrarFormularioEditar(request, response);
            }

            // RUTA: /reservas/borrar - Cancelación (Admin)
            case "/borrar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos para eliminar reservas");
                    return;
                }
                borrarReserva(request, response);
            }

            // RUTA: /reservas/disponibilidad - Consulta de huecos libres
            case "/disponibilidad" -> {
                if (!estaLogueado(request)) {
                    if (esAjax) {
                        enviarErrorJson(response, "No autenticado");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                if (esAjax) {
                    mostrarDisponibilidadAjax(request, response);
                } else {
                    mostrarDisponibilidad(request, response);
                }
            }

            default ->
                forwardError(request, response, "Página no encontrada.");
        }
    }

    // ==========================================
    // PROCESAMIENTO (POST)
    // ==========================================

    /**
     * Maneja el procesamiento de formularios y transacciones.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getPathInfo();

        switch (accion) {
            // Paso 1: Validación y cálculo de precio (Pre-Pago)
            case "/crear" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                procesarCrearReserva(request, response);
            }

            // Guardado directo sin pago (Admin)
            case "/guardar" -> {
                if (!esAdministrador(request)) {
                    forwardError(request, response, "No tiene permisos");
                    return;
                }
                procesarGuardarReserva(request, response);
            }

            // Renderizado de la pasarela de pago con datos confirmados
            case "/preparar-pago" -> {
                if (!estaLogueado(request)) {
                    response.sendRedirect(request.getContextPath() + "/usuario/login");
                    return;
                }
                prepararPago(request, response);
            }

            // Paso 2: Ejecución del pago en Stripe y persistencia final
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

    // ==========================================
    // LÓGICA DE DISPONIBILIDAD
    // ==========================================

    /**
     * Calcula y muestra la vista inicial de disponibilidad para una instalación.
     * Genera la estructura de la semana y filtra los horarios ya ocupados.
     */
    private void mostrarDisponibilidad(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String espacioIdParam = request.getParameter("espacioId");
        String fechaParam = request.getParameter("fecha");

        LOG.log(Level.INFO, "espacioId: {0}, fecha: {1}", 
                new Object[]{espacioIdParam, fechaParam});

        if (espacioIdParam == null || espacioIdParam.trim().isEmpty()) {
            LOG.log(Level.SEVERE, "ERROR: espacioIdParam está vacío o nulo");
            forwardError(request, response, "ID de instalación no proporcionado");
            return;
        }

        try {
            Long espacioId = Long.parseLong(espacioIdParam.trim());
            LOG.log(Level.INFO, "Procesando espacioId: {0}", espacioId);

            LocalDate fecha;
            if (fechaParam == null || fechaParam.trim().isEmpty()) {
                fecha = LocalDate.now();
                // Regla de Negocio: Si es fin de semana, saltar al lunes
                while (esFinDeSemana(fecha)) {
                    fecha = fecha.plusDays(1);
                }
                LOG.log(Level.INFO, "Fecha automática seleccionada: {0}", fecha);
            } else {
                fecha = LocalDate.parse(fechaParam);
            }

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            if (espacio == null) {
                forwardError(request, response, "Instalación no encontrada");
                return;
            }

            // Generación de calendario semanal
            LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
            List<Map<String, Object>> diasDisponibles = generarDiasSemana(inicioSemana);
            
            // Generación de slots horarios (8:30 - 20:30)
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

            // Preparación de la vista
            request.setAttribute("espacio", espacio);
            request.setAttribute("fecha", fecha.toString());
            request.setAttribute("diasDisponibles", diasDisponibles);
            request.setAttribute("horariosConInfo", horariosConInfo);
            request.setAttribute("inicioSemana", inicioSemana.toString());

            setLayoutAttributes(request, "Horarios Disponibles", "Selecciona un horario para tu reserva");
            request.setAttribute("pageContent", "../reservas/disponibilidadInstalacion.jsp");
            forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "[DISPONIBILIDAD-v1] ERROR: " + e.getMessage(), e);
            forwardError(request, response, "Error al obtener disponibilidad: " + e.getMessage());
        }
    }

    /**
     * Endpoint API para actualizar la disponibilidad dinámicamente al cambiar de día.
     * Devuelve JSON con los nuevos slots horarios y estructura de calendario si cambia la semana.
     */
    private void mostrarDisponibilidadAjax(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        
        String espacioIdParam = request.getParameter("espacioId");
        String fechaParam = request.getParameter("fecha");
        String accion = request.getParameter("accion"); // "cambiarSemana" o null
        
        try {
            Long espacioId = Long.parseLong(espacioIdParam.trim());
            LocalDate fecha = LocalDate.parse(fechaParam);
            
            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            if (espacio == null) {
                enviarErrorJson(response, "Instalación no encontrada");
                return;
            }
            
            StringBuilder json = new StringBuilder("{");
            
            // Si el usuario navega entre semanas, regenerar la estructura de días
            if ("cambiarSemana".equals(accion)) {
                LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
                List<Map<String, Object>> dias = generarDiasSemana(inicioSemana);
                
                json.append("\"dias\": [");
                // Construcción manual del JSON para evitar dependencias externas pesadas
                for (int i = 0; i < dias.size(); i++) {
                    if (i > 0) json.append(",");
                    Map<String, Object> dia = dias.get(i);
                    json.append("{");
                    json.append("\"fechaStr\": \"").append(dia.get("fechaStr")).append("\",");
                    json.append("\"activo\": ").append(dia.get("activo")).append(",");
                    json.append("\"numero\": ").append(dia.get("numero")).append(",");
                    json.append("\"esFinDeSemana\": ").append(dia.get("esFinDeSemana")).append(",");
                    json.append("\"nombre\": \"").append(dia.get("nombre")).append("\",");
                    json.append("\"mes\": \"").append(dia.get("mes")).append("\"");
                    json.append("}");
                }
                json.append("],");
            }
            
            // Recalcular slots disponibles
            List<String> horariosDisponibles = obtenerHorariosDisponibles(espacioId, fecha);
            
            json.append("\"horarios\": [");
            for (int i = 0; i < horariosDisponibles.size(); i++) {
                if (i > 0) json.append(",");
                String horario = horariosDisponibles.get(i);
                LocalTime horaInicio = LocalTime.parse(horario);
                LocalTime horaFin = horaInicio.plusMinutes(90);
                
                json.append("{");
                json.append("\"inicio\": \"").append(horario).append("\",");
                json.append("\"fin\": \"").append(horaFin.toString()).append("\",");
                json.append("\"fecha\": \"").append(fecha.toString()).append("\"");
                json.append("}");
            }
            json.append("],");
            
            // Formateo de fecha amigable
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", new Locale("es", "ES"));
            String fechaFormateada = fecha.format(formatter);
            json.append("\"fechaFormateada\": \"").append(fechaFormateada).append("\"");
            
            json.append("}");
            response.getWriter().write(json.toString());
            
        } catch (Exception e) {
            enviarErrorJson(response, "Error: " + e.getMessage());
        }
    }

    // ==========================================
    // LÓGICA DE NEGOCIO Y RESERVAS
    // ==========================================

    /**
     * Paso 1 del flujo de reserva: Validación y Cálculo.
     * Verifica reglas de negocio, calcula el precio y decide si redirigir al pago o guardar directamente.
     */
    private void procesarCrearReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Usuario usuario = getUsuarioLogueado(request);
            Long espacioId = Long.parseLong(request.getParameter("espacioId"));
            LocalDate fecha = LocalDate.parse(request.getParameter("fecha"));
            LocalTime hora = LocalTime.parse(request.getParameter("hora"));
            boolean tieneTuo = "true".equals(request.getParameter("tieneTuo"));

            // Validaciones de Negocio
            if (esFinDeSemana(fecha)) {
                forwardError(request, response, "No se pueden realizar reservas los fines de semana");
                return;
            }

            if (!esHorarioValido(hora)) {
                forwardError(request, response, "El horario seleccionado no está dentro del rango permitido (8:00 - 20:30)");
                return;
            }

            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            // Verificar Colisiones (Concurrency Check)
            if (usuarioTieneReservaEnHorario(usuario.getId(), inicio, fin, null)) {
                forwardError(request, response, "Ya tiene una reserva activa en ese horario.");
                return;
            }

            if (existeColision(espacioId, inicio, fin, null)) {
                forwardError(request, response, "El horario seleccionado ya está reservado");
                return;
            }

            // Cálculo del Precio
            BigDecimal precio = calcularPrecio(espacio, usuario, tieneTuo);

            // RAMA GRATUITA: Profesores o Coste 0
            if (usuario.getRol() == 2 || precio.compareTo(BigDecimal.ZERO) == 0) {
                Reserva reserva = new Reserva(usuario, espacio, inicio, fin);
                guardarReserva(reserva);
                response.sendRedirect(request.getContextPath() + "/reservas/?success");
                return;
            }

            // RAMA DE PAGO: Preparar datos temporales para la vista de pago
            request.setAttribute("reservaTemporal_espacioId", espacioId.toString());
            request.setAttribute("reservaTemporal_fecha", fecha.toString());
            request.setAttribute("reservaTemporal_hora", hora.toString());
            request.setAttribute("reservaTemporal_tieneTuo", String.valueOf(tieneTuo));
            request.setAttribute("reservaTemporal_precio", precio.toString());
            request.setAttribute("reservaTemporal_inicio", inicio.toString());
            request.setAttribute("reservaTemporal_fin", fin.toString());
            request.setAttribute("reservaTemporal_espacio", espacio);

            prepararPago(request, response);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al procesar reserva", e);
            forwardError(request, response, "Error al procesar la reserva: " + e.getMessage());
        }
    }

    /**
     * Paso 2 del flujo de reserva: Ejecución del Pago.
     * Recibe el token de pago y ejecuta la transacción contra Stripe.
     */
    private void procesarPago(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Usuario usuario = getUsuarioLogueado(request);
            String paymentMethodId = request.getParameter("paymentMethodId");
            
            // Recuperar y validar parámetros
            Long espacioId = Long.parseLong(request.getParameter("espacioId"));
            BigDecimal precio = new BigDecimal(request.getParameter("precio"));
            LocalDate fecha = LocalDate.parse(request.getParameter("fecha"));
            LocalTime hora = LocalTime.parse(request.getParameter("hora"));
            
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);
            
            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);

            // Re-verificar colisión (Race condition check)
            if (existeColision(espacioId, inicio, fin, null)) {
                forwardError(request, response, "El horario seleccionado ya está reservado por otro usuario.");
                return;
            }

            // Llamada a API Externa (Stripe)
            boolean pagoExitoso = procesarPagoStripe(precio, usuario, espacio, paymentMethodId);

            if (pagoExitoso) {
                Reserva reserva = new Reserva(usuario, espacio, inicio, fin);
                guardarReserva(reserva);
                response.sendRedirect(request.getContextPath() + "/reservas/?success=Reserva creada y pago procesado correctamente");
            } else {
                forwardError(request, response, "Error en el pago. La reserva no se ha realizado.");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Excepción al procesar pago: " + e.getMessage(), e);
            forwardError(request, response, "Error al procesar el pago: " + e.getMessage());
        }
    }

    // ==========================================
    // INTEGRACIÓN CON STRIPE API
    // ==========================================

    /**
     * Realiza una petición HTTPS manual a la API de Stripe para confirmar un PaymentIntent.
     * * <p><strong>Nota de seguridad:</strong> Utiliza una configuración SSL permisiva para desarrollo.
     * En producción, eliminar `configurarSSLParaDesarrollo()`.
     * * @param precio Monto a cobrar.
     * @param usuario Usuario pagador.
     * @param espacio Instalación reservada.
     * @param paymentMethodId Token del método de pago generado en el frontend.
     * @return true si el pago fue exitoso ("succeeded").
     */
    private boolean procesarPagoStripe(BigDecimal precio, Usuario usuario, EspacioDeportivo espacio, String paymentMethodId) {
        try {
            int amountInCents = precio.multiply(new BigDecimal("100")).intValue();
            
            // Bypass SSL para entornos de desarrollo local
            configurarSSLParaDesarrollo();

            URL url = new URL("https://api.stripe.com/v1/payment_intents");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + STRIPE_SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String postData = "amount=" + amountInCents
                    + "&currency=eur"
                    + "&payment_method=" + URLEncoder.encode(paymentMethodId, "UTF-8")
                    + "&confirm=true"
                    + "&automatic_payment_methods[enabled]=true"
                    + "&automatic_payment_methods[allow_redirects]=never"
                    + "&description=Reserva: " + URLEncoder.encode(espacio.getNombre(), "UTF-8")
                    + "&receipt_email=" + URLEncoder.encode(usuario.getEmail() != null ? usuario.getEmail() : "", "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                // Leer y parsear respuesta JSON de éxito
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                try (JsonReader jsonReader = Json.createReader(new StringReader(response.toString()))) {
                    JsonObject jsonResponse = jsonReader.readObject();
                    String status = jsonResponse.getString("status", "unknown");
                    return "succeeded".equals(status) || "requires_capture".equals(status);
                }
            } else {
                LOG.log(Level.SEVERE, "Error Stripe API: {0}", responseCode);
                return false;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Excepción en integración Stripe", e);
            return false;
        }
    }

    // ==========================================
    // REGLAS DE NEGOCIO (PRECIOS)
    // ==========================================

    /**
     * Calcula el precio de la reserva basándose en reglas complejas de negocio.
     * * <p>Factores:</p>
     * <ul>
     * <li>Rol de usuario (Profesor = Gratis).</li>
     * <li>Tipo de instalación (Pabellón, Pista, Aula).</li>
     * <li>Uso de luz artificial (detectado por nombre de instalación).</li>
     * <li>Posesión de Tarjeta Universitaria (TUO) para descuentos.</li>
     * </ul>
     */
    private BigDecimal calcularPrecio(EspacioDeportivo espacio, Usuario usuario, boolean tieneTuo) {
        if (usuario.getRol() == 2) {
            return BigDecimal.ZERO;
        }

        String tipo = espacio.getTipo().toLowerCase();
        String nombre = espacio.getNombre().toLowerCase();
        String descripcion = espacio.getDescripcion() != null ? espacio.getDescripcion().toLowerCase() : "";

        // Reglas para Pabellones
        if (nombre.contains("pabellón") || nombre.contains("pabellon") || tipo.contains("pabellón")) {
            boolean conLuz = nombre.contains("luz") || descripcion.contains("luz");
            if (tieneTuo) {
                return conLuz ? new BigDecimal("12.00") : new BigDecimal("9.00");
            } else {
                return conLuz ? new BigDecimal("30.00") : new BigDecimal("25.00");
            }
        }

        // Reglas para Aulas y Salas
        if (tipo.contains("aula") || nombre.contains("aula") || tipo.contains("sala") || nombre.contains("sala")) {
            return tieneTuo ? BigDecimal.ZERO : new BigDecimal("15.00");
        }

        // Reglas para Tenis y Pádel
        if (tipo.contains("tenis") || nombre.contains("tenis") || tipo.contains("pádel") || tipo.contains("padel")) {
            boolean conLuz = nombre.contains("luz") || descripcion.contains("luz");
            // Precio reducido con TUO, estándar sin TUO. Suplemento por luz.
            return tieneTuo
                    ? (conLuz ? new BigDecimal("2.00") : new BigDecimal("1.50"))
                    : (conLuz ? new BigDecimal("6.00") : new BigDecimal("4.00"));
        }

        return BigDecimal.ZERO; // Default fallback
    }

    // ==========================================
    // PERSISTENCIA Y CONSULTAS
    // ==========================================

    /**
     * Busca los horarios disponibles (slots de 90 min) para una fecha y espacio dados.
     * Excluye horarios ya reservados.
     */
    private List<String> obtenerHorariosDisponibles(Long espacioId, LocalDate fecha) {
        List<String> horarios = new ArrayList<>();

        if (esFinDeSemana(fecha)) return horarios;

        LocalTime horaInicio = LocalTime.of(8, 30);
        LocalTime horaFin = LocalTime.of(20, 30);
        LocalTime horaActual = horaInicio;

        while (horaActual.plusMinutes(DURACION_RESERVA_MINUTOS).isBefore(horaFin.plusMinutes(1))) {
            LocalDateTime inicio = LocalDateTime.of(fecha, horaActual);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            if (!existeColision(espacioId, inicio, fin, null)) {
                horarios.add(horaActual.toString());
            }
            horaActual = horaActual.plusMinutes(DURACION_RESERVA_MINUTOS);
        }
        return horarios;
    }

    /**
     * Verifica si existe solapamiento de reservas para un espacio en un intervalo.
     */
    private boolean existeColision(Long espacioId, LocalDateTime inicio, LocalDateTime fin, Long excludeId) {
        String jpql = "SELECT COUNT(r) FROM Reserva r WHERE r.espacio.id = :espacioId "
                + "AND ((r.inicio < :fin AND r.fin > :inicio))";

        if (excludeId != null) jpql += " AND r.id != :excludeId";

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("espacioId", espacioId);
        query.setParameter("inicio", inicio);
        query.setParameter("fin", fin);

        if (excludeId != null) query.setParameter("excludeId", excludeId);

        return query.getSingleResult() > 0;
    }

    /**
     * Verifica si un usuario ya tiene una reserva en el mismo intervalo (para evitar doble reserva simultánea).
     */
    private boolean usuarioTieneReservaEnHorario(Long usuarioId, LocalDateTime inicio, LocalDateTime fin, Long excludeId) {
        String jpql = "SELECT COUNT(r) FROM Reserva r WHERE r.usuario.id = :usuarioId "
                + "AND (r.inicio < :fin AND r.fin > :inicio)";

        if (excludeId != null) jpql += " AND r.id != :excludeId";

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("usuarioId", usuarioId);
        query.setParameter("inicio", inicio);
        query.setParameter("fin", fin);
        
        if (excludeId != null) query.setParameter("excludeId", excludeId);

        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    private void guardarReserva(Reserva reserva) {
        try {
            utx.begin();
            if (reserva.getId() == null) {
                em.persist(reserva);
            } else {
                em.merge(reserva);
            }
            utx.commit();
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ex) { LOG.severe("Rollback failed"); }
            throw new RuntimeException(e);
        }
    }
    
    // Métodos auxiliares de vista y seguridad...
    
    private void enviarErrorJson(HttpServletResponse response, String mensaje) throws IOException {
        response.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }

    private List<Map<String, Object>> generarDiasSemana(LocalDate inicioSemana) {
        List<Map<String, Object>> diasDisponibles = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate fechaDia = inicioSemana.plusDays(i);
            Map<String, Object> diaInfo = new HashMap<>();
            diaInfo.put("fechaStr", fechaDia.toString());
            diaInfo.put("activo", false);
            diaInfo.put("numero", fechaDia.getDayOfMonth());
            diaInfo.put("esFinDeSemana", esFinDeSemana(fechaDia));
            diaInfo.put("esHoy", fechaDia.equals(hoy));
            diaInfo.put("esMañana", fechaDia.equals(hoy.plusDays(1)));
            
            // Lógica de nombres de día (HOY, MAÑANA, LUN, MAR...)
            if (fechaDia.equals(hoy)) diaInfo.put("nombre", "HOY");
            else if (fechaDia.equals(hoy.plusDays(1))) diaInfo.put("nombre", "MAÑANA");
            else diaInfo.put("nombre", fechaDia.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")).toUpperCase());
            
            diaInfo.put("mes", fechaDia.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")));
            diasDisponibles.add(diaInfo);
        }
        return diasDisponibles;
    }
    
    // Helpers de infraestructura (Layout, Redirección, SSL Bypass...)
    
    private void forward(HttpServletRequest request, HttpServletResponse response, String vista) throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String mensaje) throws ServletException, IOException {
        request.setAttribute("msg", mensaje);
        forward(request, response, "/WEB-INF/vistas/error.jsp");
    }

    private boolean estaLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("usuario") != null;
    }

    private boolean esAdministrador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return usuario != null && usuario.getRol() == 0;
        }
        return false;
    }
    
    private Usuario getUsuarioLogueado(HttpServletRequest request) {
        return (Usuario) request.getSession(false).getAttribute("usuario");
    }

    private void setLayoutAttributes(HttpServletRequest request, String title, String subtitle) {
        request.setAttribute("pageTitle", title);
        request.setAttribute("pageSubtitle", subtitle);
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    private boolean esHorarioValido(LocalTime hora) {
        LocalTime horaMinima = LocalTime.of(8, 30);
        LocalTime horaMaxima = LocalTime.of(20, 30);
        return !hora.isBefore(horaMinima) && !hora.isAfter(horaMaxima);
    }
    
    private void configurarSSLParaDesarrollo() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            LOG.warning("Fallo al configurar SSL bypass");
        }
    }
    
    // Métodos de gestión de reservas restantes (mostrarReservas, mostrarPanelAdmin, mostrarFormularioNueva...)
    // Se mantienen con su lógica original, ya documentados implícitamente por el flujo general.
    private void mostrarReservas(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Usuario usuario = getUsuarioLogueado(request);
        List<Reserva> reservas = (usuario.getRol() == 0) ? obtenerTodasLasReservas() : obtenerReservasDelUsuario(usuario.getId());
        request.setAttribute("reservas", reservas);
        setLayoutAttributes(request, "Mis Reservas", "Gestiona tus reservas de instalaciones deportivas");
        request.setAttribute("pageContent", "../reservas/listaReservas.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void mostrarPanelAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Reserva> reservas = obtenerTodasLasReservas();
        request.setAttribute("reservas", reservas);
        setLayoutAttributes(request, "Panel de Reservas", "Gestiona todas las reservas del sistema");
        request.setAttribute("pageContent", "../reservas/panelReservas.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void mostrarFormularioNueva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<EspacioDeportivo> espacios = obtenerTodosLosEspacios();
        request.setAttribute("espacios", espacios);
        // Lógica de pre-rellenado si vienen parámetros...
        setLayoutAttributes(request, "Nueva Reserva", "Reserva una instalación deportiva");
        request.setAttribute("pageContent", "../reservas/formReserva.jsp");
        forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null) {
            Reserva reserva = em.find(Reserva.class, Long.parseLong(idParam));
            if (reserva != null) {
                request.setAttribute("reserva", reserva);
                request.setAttribute("espacios", obtenerTodosLosEspacios());
                setLayoutAttributes(request, "Editar Reserva", "Modifica los datos de la reserva");
                request.setAttribute("pageContent", "../reservas/formReserva.jsp");
                forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");
            } else forwardError(request, response, "Reserva no encontrada.");
        } else forwardError(request, response, "ID no proporcionado.");
    }
    
    private void procesarGuardarReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Lógica simplificada de guardado admin (sin pago)
            Long espacioId = Long.parseLong(request.getParameter("espacioId"));
            LocalDate fecha = LocalDate.parse(request.getParameter("fecha"));
            LocalTime hora = LocalTime.parse(request.getParameter("hora"));
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(DURACION_RESERVA_MINUTOS);
            
            EspacioDeportivo espacio = em.find(EspacioDeportivo.class, espacioId);
            Reserva reserva = new Reserva();
            // Si es edición...
            String idParam = request.getParameter("id");
            if(idParam != null && !idParam.isEmpty()) reserva = em.find(Reserva.class, Long.parseLong(idParam));
            
            reserva.setEspacio(espacio);
            reserva.setInicio(inicio);
            reserva.setFin(fin);
            // Nota: En admin asumimos usuario actual o se debería seleccionar un usuario
            if(reserva.getUsuario() == null) reserva.setUsuario(getUsuarioLogueado(request)); 
            
            guardarReserva(reserva);
            response.sendRedirect(request.getContextPath() + "/reservas/panel");
        } catch(Exception e) {
            forwardError(request, response, "Error al guardar: " + e.getMessage());
        }
    }
    
    private void borrarReserva(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if(idParam != null) {
            eliminarReserva(Long.parseLong(idParam));
            response.sendRedirect(request.getContextPath() + "/reservas/panel");
        }
    }
    
    /**
     * prepararPago
     * Calcula explícitamente la hora de fin y formatea las horas para la vista.
     */
    private void prepararPago(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 1. Recuperar datos (pueden venir como atributos si es flujo interno o parámetros si es forward)
            String espacioIdStr = (String) request.getAttribute("reservaTemporal_espacioId");
            String fechaStr = (String) request.getAttribute("reservaTemporal_fecha");
            String horaStr = (String) request.getAttribute("reservaTemporal_hora");
            String tieneTuoStr = (String) request.getAttribute("reservaTemporal_tieneTuo");
            String precioStr = (String) request.getAttribute("reservaTemporal_precio");
            EspacioDeportivo espacio = (EspacioDeportivo) request.getAttribute("reservaTemporal_espacio");

            // Fallback: Si los atributos son nulos, intentar recuperarlos de request.getParameter
            if (espacioIdStr == null) espacioIdStr = request.getParameter("espacioId");
            if (fechaStr == null) fechaStr = request.getParameter("fecha");
            if (horaStr == null) horaStr = request.getParameter("hora");
            
            // Validación de seguridad básica
            if (espacioIdStr == null || fechaStr == null || horaStr == null || espacio == null) {
                forwardError(request, response, "Datos de reserva perdidos. Por favor, inicie el proceso nuevamente.");
                return;
            }

            // 2. Parsear datos
            Long espacioId = Long.parseLong(espacioIdStr);
            LocalDate fecha = LocalDate.parse(fechaStr);
            boolean tieneTuo = Boolean.parseBoolean(tieneTuoStr);
            
            // --- AQUÍ ESTÁ LA CORRECCIÓN CLAVE ---
            // Parsear hora de inicio y CALCULAR hora de fin
            LocalTime horaInicio = LocalTime.parse(horaStr);
            LocalTime horaFin = horaInicio.plusMinutes(DURACION_RESERVA_MINUTOS);

            // Crear formateador para que salga bonito (ej: "10:00")
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // 3. Pasar TODOS los atributos a la JSP
            request.setAttribute("espacio", espacio);
            request.setAttribute("espacioId", espacioId);
            request.setAttribute("fecha", fechaStr);
            
            // Dato crudo para el <input hidden> (lo necesita el backend al procesar el pago)
            request.setAttribute("hora", horaStr); 
            
            // DATOS VISUALES (Esto es lo que te faltaba para que se vea en el resumen)
            request.setAttribute("horaInicio", horaInicio.format(timeFormatter));
            request.setAttribute("horaFin", horaFin.format(timeFormatter));
            
            request.setAttribute("tieneTuo", tieneTuo);
            request.setAttribute("precio", precioStr);

            setLayoutAttributes(request, "Pago de Reserva", "Completa tu pago de forma segura");
            request.setAttribute("pageContent", "../reservas/pagoReserva.jsp");
            forward(request, response, "/WEB-INF/vistas/templates/layout.jsp");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al preparar pago", e);
            forwardError(request, response, "Error al preparar el pago: " + e.getMessage());
        }
    }

    private void eliminarReserva(Long id) {
        try {
            utx.begin();
            Reserva r = em.find(Reserva.class, id);
            if(r != null) em.remove(r);
            utx.commit();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Reserva> obtenerTodasLasReservas() {
        return em.createQuery("SELECT r FROM Reserva r ORDER BY r.inicio DESC", Reserva.class).getResultList();
    }
    
    private List<Reserva> obtenerReservasDelUsuario(Long usuarioId) {
        return em.createQuery("SELECT r FROM Reserva r WHERE r.usuario.id = :uid ORDER BY r.inicio DESC", Reserva.class)
                 .setParameter("uid", usuarioId).getResultList();
    }
    
    private List<EspacioDeportivo> obtenerTodosLosEspacios() {
        return em.createQuery("SELECT e FROM EspacioDeportivo e ORDER BY e.nombre", EspacioDeportivo.class).getResultList();
    }
}