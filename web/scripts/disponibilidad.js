/**
 * SCRIPT: GESTIÓN DE DISPONIBILIDAD Y HORARIOS
 * Maneja la lógica del calendario, carga asíncrona de horarios y navegación entre semanas.
 * Requiere que se defina window.ReservaConfig antes de cargar este script.
 * * @author agustinrodriguez
 */

// Configuración global y estado
let ESPACIO_ID = null;
let CONTEXT_PATH = '';
let fechaActual = '';
let inicioSemanaActual = '';

/**
 * Inicializa las variables globales basándose en la configuración inyectada desde el JSP
 */
function inicializarVariables() {
    if (window.ReservaConfig) {
        CONTEXT_PATH = window.ReservaConfig.contextPath || '';
        fechaActual = window.ReservaConfig.fechaActual || '';
        inicioSemanaActual = window.ReservaConfig.inicioSemana || '';
        
        // Intentar obtener el ID de la forma más robusta posible
        ESPACIO_ID = obtenerEspacioIdDefinitivo();
    } else {
        console.error("Error crítico: window.ReservaConfig no está definido.");
    }

    console.log('Configuración inicializada:');
    console.log('ESPACIO_ID:', ESPACIO_ID);
    console.log('CONTEXT_PATH:', CONTEXT_PATH);
}

/**
 * Determina el ID del espacio buscando en varias fuentes (Config, Data Attribute, URL)
 */
function obtenerEspacioIdDefinitivo() {
    // 1. Desde configuración JSP
    if (window.ReservaConfig && window.ReservaConfig.espacioId) {
        return window.ReservaConfig.espacioId;
    }

    // 2. Desde atributo data del contenedor
    const container = document.querySelector('.reservas-container');
    if (container) {
        const dataId = container.getAttribute('data-espacio-id');
        if (dataId) return dataId;
    }

    // 3. Desde la URL
    const urlParams = new URLSearchParams(window.location.search);
    const urlId = urlParams.get('espacioId');
    if (urlId) return urlId;

    return null;
}

/**
 * Carga los horarios disponibles para una fecha específica mediante AJAX
 */
function cargarDia(fecha) {
    if (!fecha || !ESPACIO_ID) {
        console.error('Faltan datos para cargar el día (Fecha o ID)');
        return;
    }

    fechaActual = fecha;

    // Actualizar UI visual (clases activas)
    document.querySelectorAll('.dia-option').forEach(dia => {
        dia.classList.toggle('active', dia.dataset.fecha === fecha);
    });

    // Mostrar estado de carga
    const loading = document.getElementById('horariosLoading');
    const grid = document.getElementById('horariosGrid');
    if (loading) loading.style.display = 'flex';
    if (grid) grid.style.opacity = '0.5';

    // Construcción segura de la URL
    const url = `${CONTEXT_PATH}/reservas/disponibilidad?espacioId=${encodeURIComponent(ESPACIO_ID)}&fecha=${encodeURIComponent(fecha)}&accion=cambiarDia&ajax=true`;

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error(`Error ${res.status}: ${res.statusText}`);
            return res.json();
        })
        .then(data => {
            if (data.error) {
                mostrarError('Error del servidor: ' + data.error);
                return;
            }

            // Actualizar título de fecha
            const tituloElement = document.getElementById('fechaSeleccionadaTexto');
            if (tituloElement && data.fechaFormateada) {
                tituloElement.textContent = 'Horarios Disponibles para ' + data.fechaFormateada;
            }

            // Renderizar grid
            actualizarHorariosGrid(data.horarios);
        })
        .catch(err => {
            console.error('Error fetch:', err);
            mostrarError('No se pudieron cargar los horarios. Intente nuevamente.');
        })
        .finally(() => {
            if (loading) loading.style.display = 'none';
            if (grid) grid.style.opacity = '1';
        });
}

/**
 * Renderiza el HTML de los horarios recibidos
 */
function actualizarHorariosGrid(horarios) {
    const grid = document.getElementById('horariosGrid');
    if (!grid) return;

    grid.innerHTML = '';

    if (horarios && horarios.length > 0) {
        let html = '';
        horarios.forEach(h => {
            const href = `${CONTEXT_PATH}/reservas/nueva?espacioId=${encodeURIComponent(ESPACIO_ID)}&fecha=${encodeURIComponent(h.fecha)}&hora=${encodeURIComponent(h.inicio)}`;
            
            html += `
                <a href="${href}" class="horario-card">
                    <div class="horario-icon"><i class="fas fa-clock"></i></div>
                    <div class="horario-info">
                        <div class="horario-time">${h.inicio} - ${h.fin}</div>
                        <div class="horario-duracion">1 hora 30 minutos</div>
                    </div>
                    <div class="horario-action"><i class="fas fa-arrow-right"></i></div>
                </a>`;
        });
        grid.innerHTML = html;
    } else {
        grid.innerHTML = `
            <div class="horarios-vacio">
                <i class="fas fa-calendar-times"></i>
                <h4>No hay horarios disponibles</h4>
                <p>Todos los horarios están reservados para este día.</p>
            </div>`;
    }
}

/**
 * Navegación entre semanas (Anterior/Siguiente)
 */
function cambiarSemana(direccion) {
    if (!ESPACIO_ID) return;

    const fecha = new Date(inicioSemanaActual);
    fecha.setDate(fecha.getDate() + (direccion * 7));
    const nuevaSemana = fecha.toISOString().split('T')[0];

    const container = document.getElementById('diasContainer');
    if (container) container.style.opacity = '0.5';

    const url = `${CONTEXT_PATH}/reservas/disponibilidad?espacioId=${encodeURIComponent(ESPACIO_ID)}&fecha=${encodeURIComponent(nuevaSemana)}&accion=cambiarSemana&ajax=true`;

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error('Error de red');
            return res.json();
        })
        .then(data => {
            if (data.error) {
                mostrarError(data.error);
                return;
            }

            // Actualizar etiqueta de rango
            if (data.rangoFechas) {
                document.getElementById('rangoFechas').textContent = data.rangoFechas;
            }

            // Renderizar días
            if (data.dias && container) {
                renderizarDias(container, data.dias);
            }

            inicioSemanaActual = nuevaSemana;

            // Cargar automáticamente el primer día disponible de la nueva semana
            const primerDia = data.dias.find(d => !d.esFinDeSemana);
            if (primerDia) {
                cargarDia(primerDia.fechaStr);
            }
        })
        .catch(err => mostrarError('Error al cambiar semana: ' + err.message))
        .finally(() => {
            if (container) container.style.opacity = '1';
        });
}

/**
 * Helper para renderizar el HTML de los días de la semana
 */
function renderizarDias(container, dias) {
    const html = dias.map(dia => {
        if (dia.esFinDeSemana) {
            return `
                <div class="dia-option disabled">
                    <div class="dia-nombre">${dia.nombre}</div>
                    <div class="dia-numero">${dia.numero}</div>
                    <div class="dia-mes">${dia.mes}</div>
                    <div class="dia-no-disponible"><i class="fas fa-ban"></i></div>
                </div>`;
        } else {
            return `
                <a href="#" class="dia-option" data-fecha="${dia.fechaStr}">
                    <div class="dia-nombre">${dia.nombre}</div>
                    <div class="dia-numero">${dia.numero}</div>
                    <div class="dia-mes">${dia.mes}</div>
                </a>`;
        }
    }).join('');

    container.innerHTML = html;
    asignarEventosDias(); // Re-asignar listeners a los nuevos elementos
}

/**
 * Asigna los eventos click a los botones de día generados dinámicamente
 */
function asignarEventosDias() {
    document.querySelectorAll('.dia-option:not(.disabled)').forEach(el => {
        el.addEventListener('click', function(e) {
            e.preventDefault();
            cargarDia(this.dataset.fecha);
        });
    });
}

/**
 * Muestra mensajes de error en la interfaz
 */
function mostrarError(mensaje) {
    const section = document.querySelector('.horarios-disponibles-section');
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger';
    errorDiv.innerHTML = `
        <strong><i class="fas fa-exclamation-circle"></i> Error:</strong> ${mensaje}
        <button onclick="this.parentElement.remove()" style="float:right;background:none;border:none;cursor:pointer;">&times;</button>
    `;
    
    // Eliminar alertas previas
    const prevAlert = section.querySelector('.alert-danger');
    if(prevAlert) prevAlert.remove();
    
    section.prepend(errorDiv);
    setTimeout(() => errorDiv.remove(), 8000);
}

// ==========================================
// INICIALIZACIÓN AL CARGAR DOM
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    inicializarVariables();

    if (!ESPACIO_ID) {
        mostrarError('Error crítico: No se ha identificado la instalación.');
        return;
    }

    // Listeners iniciales
    asignarEventosDias();

    const btnPrev = document.getElementById('btnSemanaAnterior');
    const btnNext = document.getElementById('btnSemanaSiguiente');

    if (btnPrev) btnPrev.addEventListener('click', (e) => { e.preventDefault(); cambiarSemana(-1); });
    if (btnNext) btnNext.addEventListener('click', (e) => { e.preventDefault(); cambiarSemana(1); });

    // Navegación por teclado
    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') cambiarSemana(-1);
        if (e.key === 'ArrowRight') cambiarSemana(1);
    });
});
