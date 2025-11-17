<%-- 
    Document   : formReserva
    Created on : 13 nov 2025
    Author     : agustinrodriguez
    
    Formulario para crear/editar reservas con integración de Stripe
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="reserva-form-container">
    <div class="reserva-form-card">
        <div class="reserva-form-header">
            <h2>
                <c:choose>
                    <c:when test="${not empty reserva}">Editar Reserva</c:when>
                    <c:otherwise>Nueva Reserva</c:otherwise>
                </c:choose>
            </h2>
            <p>Completa los datos para realizar tu reserva</p>
        </div>

        <form action="${pageContext.request.contextPath}/reservas/${not empty reserva ? 'guardar' : 'crear'}" 
              method="post" id="reservaForm">

            <c:if test="${not empty reserva}">
                <input type="hidden" name="id" value="${reserva.id}">
            </c:if>

            <div class="reserva-form-steps">
                <!-- PASO 1: Seleccionar Instalación -->
                <div class="reserva-form-step">
                    <div class="reserva-form-step-header">
                        <div class="reserva-step-number">1</div>
                        <h3 class="reserva-step-title">Selecciona la Instalación</h3>
                    </div>
                    <div class="reserva-step-content">
                        <div class="form-group">
                            <label for="espacioId">Instalación Deportiva *</label>
                            <div class="input-wrapper">
                                <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                                </svg>
                                <select id="espacioId" name="espacioId" required>
                                    <option value="">Selecciona una instalación</option>
                                    <c:forEach var="espacio" items="${espacios}">
                                        <option value="${espacio.id}" 
                                                data-nombre="${espacio.nombre}"
                                                data-tipo="${espacio.tipo}"
                                                ${not empty reserva && reserva.espacio.id == espacio.id ? 'selected' : ''}
                                                ${espacioIdPreseleccionado == espacio.id ? 'selected' : ''}>
                                            ${espacio.nombre} - ${espacio.tipo}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PASO 2: Seleccionar Fecha y Hora -->
                <div class="reserva-form-step">
                    <div class="reserva-form-step-header">
                        <div class="reserva-step-number">2</div>
                        <h3 class="reserva-step-title">Fecha y Horario</h3>
                    </div>
                    <div class="reserva-step-content">
                        <div class="form-row">
                            <div class="form-group">
                                <label for="fecha">Fecha *</label>
                                <div class="input-wrapper">
                                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                                    </svg>
                                    <input type="date" id="fecha" name="fecha" required
                                           value="${not empty reserva ? reserva.inicio.toLocalDate() : fechaPreseleccionada}"
                                           min="<%= java.time.LocalDate.now()%>"
                                           onchange="validarFinDeSemana()">
                                </div>
                                <small class="form-hint" id="fechaHint">Selecciona un día entre semana</small>
                            </div>

                            <div class="form-group">
                                <label for="hora">Hora de Inicio *</label>
                                <div class="input-wrapper">
                                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                    </svg>
                                    <input type="time" id="hora" name="hora" required
                                           value="${not empty reserva ? reserva.inicio.toLocalTime() : horaPreseleccionada}"
                                           min="08:30" max="19:00">
                                </div>
                                <small class="form-hint">
                                    Duración: 1 hora y 30 minutos (de ${not empty horaPreseleccionada ? horaPreseleccionada : '8:30'} a ${not empty horaFinPreseleccionada ? horaFinPreseleccionada : '20:30'})
                                </small>
                                <small class="form-hint" id="horaError" style="color:#dc2626; display:none;"></small>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PASO 3: Información de Pago (solo para nuevas reservas y no admin/profesor) -->
                <c:if test="${empty reserva && sessionScope.usuario.rol != 0 && sessionScope.usuario.rol != 2}">
                    <div class="reserva-form-step">
                        <div class="reserva-form-step-header">
                            <div class="reserva-step-number">3</div>
                            <h3 class="reserva-step-title">Información de Pago</h3>
                        </div>
                        <div class="reserva-step-content">
                            <div class="tuo-checkbox-container">
                                <label class="tuo-checkbox">
                                    <input type="checkbox" id="tieneTuo" name="tieneTuo" value="true">
                                    <div class="tuo-checkbox-label">
                                        <strong>Tengo Tarjeta Universitaria (TUO)</strong>
                                        <span>Marca esta opción si eres estudiante con TUO para obtener descuento</span>
                                    </div>
                                </label>
                            </div>

                            <div class="reserva-precio-info" id="precioInfo" style="display: none;">
                                <div class="reserva-precio-header">
                                    <div class="reserva-precio-icon">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                        </svg>
                                    </div>
                                    <h4 class="reserva-precio-title">Total a Pagar</h4>
                                </div>
                                <div class="reserva-precio-amount" id="precioAmount">0.00 €</div>
                                <p class="reserva-precio-descripcion" id="precioDescripcion"></p>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>

            <!-- Botones de Acción -->
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/reservas/" class="btn-secondary">
                    Cancelar
                </a>
                <button type="submit" class="btn-primary" id="submitBtn">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <c:choose>
                        <c:when test="${not empty reserva}">Guardar Cambios</c:when>
                        <c:otherwise><span id="btnText">Continuar</span></c:otherwise>
                    </c:choose>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    // Precios según la tabla proporcionada (por si los sigues usando en nuevas reservas)
    const preciosInstalaciones = {
        'pabellon': {
            conTuo: {sinLuz: 9.00, conLuz: 12.00},
            sinTuo: {sinLuz: 25.00, conLuz: 30.00}
        },
        'aula': {
            conTuo: 0,
            sinTuo: 15.00
        },
        'sala': {
            conTuo: 0,
            sinTuo: 15.00
        },
        'tenis': {
            conTuo: {sinLuz: 1.50, conLuz: 2.00},
            sinTuo: {sinLuz: 4.00, conLuz: 6.00}
        },
        'padel': {
            conTuo: {sinLuz: 1.50, conLuz: 2.00},
            sinTuo: {sinLuz: 4.00, conLuz: 6.00}
        }
    };

    const espacioSelect     = document.getElementById('espacioId');
    const tieneTuoCheckbox  = document.getElementById('tieneTuo');
    const precioInfo        = document.getElementById('precioInfo');
    const precioAmount      = document.getElementById('precioAmount');
    const precioDescripcion = document.getElementById('precioDescripcion');
    const btnText           = document.getElementById('btnText');
    const fechaInput        = document.getElementById('fecha');
    const fechaHint         = document.getElementById('fechaHint');
    const submitBtn         = document.getElementById('submitBtn');
    const horaInput         = document.getElementById('hora');
    const horaError         = document.getElementById('horaError');
    const reservaForm       = document.getElementById('reservaForm');

    const esProfesor = ${sessionScope.usuario.rol == 2};
    const esAdmin    = ${sessionScope.usuario.rol == 0};
    const esEdicion  = ${not empty reserva};

    // Flags de validación global
    let fechaEsValida = true;
    let horaEsValida  = true;

    // Horas válidas permitidas
    const horasValidas = [
        "08:30",
        "10:00",
        "11:30",
        "13:00",
        "14:30",
        "16:00",
        "17:30",
        "19:00"
    ];

    function updateSubmitState() {
        if (fechaEsValida && horaEsValida) {
            submitBtn.disabled = false;
            submitBtn.style.opacity = '1';
            submitBtn.style.cursor = 'pointer';
        } else {
            submitBtn.disabled = true;
            submitBtn.style.opacity = '0.6';
            submitBtn.style.cursor = 'not-allowed';
        }
    }

    // Función para validar fin de semana
    function validarFinDeSemana() {
        if (fechaInput && fechaInput.value) {
            const fecha = new Date(fechaInput.value);
            const diaSemana = fecha.getDay(); // 0=Domingo, 6=Sábado

            if (diaSemana === 0 || diaSemana === 6) {
                fechaHint.innerHTML = '<span style="color: #dc2626;">No se pueden realizar reservas los fines de semana</span>';
                fechaEsValida = false;
            } else {
                fechaHint.innerHTML = 'Día válido';
                fechaEsValida = true;
            }
        } else {
            // si no hay fecha, consideramos inválido
            fechaEsValida = false;
        }
        updateSubmitState();
    }

    // Función para validar la hora contra la lista de horas válidas
    function validarHora() {
        if (horaInput && horaInput.value) {
            // nos aseguramos de tener formato HH:MM
            let valor = horaInput.value;
            // algunos navegadores pueden poner segundos, nos quedamos con los 5 primeros
            if (valor.length >= 5) {
                valor = valor.substring(0, 5);
            }

            if (horasValidas.indexOf(valor) === -1) {
                horaError.style.display = 'inline';
                horaError.textContent = 'Hora no válida. Las horas permitidas son: 08:30, 10:00, 11:30, 13:00, 14:30, 16:00, 17:30 y 19:00.';
                horaEsValida = false;
            } else {
                horaError.style.display = 'none';
                horaError.textContent = '';
                horaEsValida = true;
            }
        } else {
            horaError.style.display = 'inline';
            horaError.textContent = 'Debe seleccionar una hora válida.';
            horaEsValida = false;
        }
        updateSubmitState();
    }

    // Función para calcular precio
    function calcularPrecio() {
        if (!espacioSelect.value || esProfesor || esAdmin || esEdicion) {
            if (precioInfo)
                precioInfo.style.display = 'none';
            if (btnText)
                btnText.textContent = 'Continuar';
            return;
        }

        const espacioOption = espacioSelect.options[espacioSelect.selectedIndex];
        const espacioTexto  = espacioOption.text.toLowerCase();
        const espacioNombre = espacioOption.getAttribute('data-nombre').toLowerCase();
        const espacioTipo   = espacioOption.getAttribute('data-tipo').toLowerCase();
        const tieneTuo      = tieneTuoCheckbox ? tieneTuoCheckbox.checked : false;

        let precio = 0;
        let descripcion = '';

        // Identificar tipo de instalación y calcular precio
        if (espacioTexto.includes('pabellón') || espacioTexto.includes('pabellon') ||
            espacioNombre.includes('pabellón') || espacioNombre.includes('pabellon')) {

            const conLuz = espacioTexto.includes('luz') || espacioNombre.includes('luz');
            if (tieneTuo) {
                precio = conLuz ? 12.00 : 9.00;
                descripcion = `Pabellón ${conLuz ? 'con luz' : 'sin luz'} - Con TUO`;
            } else {
                precio = conLuz ? 30.00 : 25.00;
                descripcion = `Pabellón ${conLuz ? 'con luz' : 'sin luz'} - Sin TUO`;
            }

        } else if (espacioTexto.includes('aula') || espacioNombre.includes('aula') || espacioTipo.includes('aula')) {

            precio = tieneTuo ? 0 : 15.00;
            descripcion = tieneTuo ? 'Aula - Gratuita con TUO' : 'Aula - Sin TUO';

        } else if (espacioTexto.includes('sala') || espacioNombre.includes('sala') || espacioTipo.includes('sala')) {

            precio = tieneTuo ? 0 : 15.00;
            descripcion = tieneTuo ? 'Sala - Gratuita con TUO' : 'Sala - Sin TUO';

        } else if (espacioTexto.includes('tenis') || espacioNombre.includes('tenis') || espacioTipo.includes('tenis')) {

            const conLuz = espacioTexto.includes('luz') || espacioNombre.includes('luz');
            precio = tieneTuo ? (conLuz ? 2.00 : 1.50) : (conLuz ? 6.00 : 4.00);
            descripcion = `Tenis ${conLuz ? 'con luz' : 'sin luz'} - ${tieneTuo ? 'Con TUO' : 'Sin TUO'}`;

        } else if (espacioTexto.includes('pádel') || espacioTexto.includes('padel') ||
                   espacioNombre.includes('pádel') || espacioNombre.includes('padel') ||
                   espacioTipo.includes('pádel') || espacioTipo.includes('padel')) {

            const conLuz = espacioTexto.includes('luz') || espacioNombre.includes('luz');
            precio = tieneTuo ? (conLuz ? 2.00 : 1.50) : (conLuz ? 6.00 : 4.00);
            descripcion = `Pádel ${conLuz ? 'con luz' : 'sin luz'} - ${tieneTuo ? 'Con TUO' : 'Sin TUO'}`;

        } else {
            precio = 0;
            descripcion = 'Instalación gratuita';
        }

        if (precioInfo) {
            precioAmount.textContent = precio.toFixed(2) + ' €';
            precioDescripcion.textContent = descripcion;
            precioInfo.style.display = 'block';
        }

        // Actualizar texto del botón
        if (btnText) {
            if (precio === 0) {
                btnText.textContent = 'Realizar Reserva Gratuita';
            } else {
                btnText.textContent = 'Continuar al Pago';
            }
        }
    }

    // Event listeners
    if (espacioSelect) {
        espacioSelect.addEventListener('change', calcularPrecio);
    }

    if (tieneTuoCheckbox) {
        tieneTuoCheckbox.addEventListener('change', calcularPrecio);
    }

    if (horaInput) {
        horaInput.addEventListener('change', validarHora);
    }

    if (fechaInput) {
        fechaInput.addEventListener('change', validarFinDeSemana);
    }

    if (reservaForm) {
        reservaForm.addEventListener('submit', function (e) {
            // última comprobación antes de enviar
            validarFinDeSemana();
            validarHora();
            if (!fechaEsValida || !horaEsValida) {
                e.preventDefault();
            }
        });
    }

    // Calcular precio inicial y validar valores iniciales
    document.addEventListener('DOMContentLoaded', function () {
        if (fechaInput && fechaInput.value) {
            validarFinDeSemana();
        } else {
            fechaEsValida = false;
            updateSubmitState();
        }

        if (horaInput && horaInput.value) {
            validarHora();
        } else {
            horaEsValida = false;
            updateSubmitState();
        }

        if (espacioSelect && espacioSelect.value) {
            calcularPrecio();
        }
    });
</script>
