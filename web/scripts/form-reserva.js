/**
 * SCRIPT: LÓGICA DEL FORMULARIO DE RESERVA
 * Gestiona el cálculo dinámico de precios, validaciones de fecha/hora
 * y el estado del botón de envío.
 * * Requiere window.ReservaConfig para los permisos de usuario.
 * * @author agustinrodriguez
 */

// Configuración de precios (Reglas de Negocio)
const PRECIOS = {
    'pabellon': {
        conTuo: { sinLuz: 9.00, conLuz: 12.00 },
        sinTuo: { sinLuz: 25.00, conLuz: 30.00 }
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
        conTuo: { sinLuz: 1.50, conLuz: 2.00 },
        sinTuo: { sinLuz: 4.00, conLuz: 6.00 }
    },
    'padel': {
        conTuo: { sinLuz: 1.50, conLuz: 2.00 },
        sinTuo: { sinLuz: 4.00, conLuz: 6.00 }
    }
};

// Horas permitidas (Bloques de 90 min)
const HORAS_VALIDAS = [
    "08:30", "10:00", "11:30", "13:00", 
    "14:30", "16:00", "17:30", "19:00"
];

// Estado de validación
let estadoValidacion = {
    fecha: false,
    hora: false
};

// Referencias al DOM
const UI = {
    espacio: document.getElementById('espacioId'),
    tuo: document.getElementById('tieneTuo'),
    precioInfo: document.getElementById('precioInfo'),
    precioAmount: document.getElementById('precioAmount'),
    precioDesc: document.getElementById('precioDescripcion'),
    btnText: document.getElementById('btnText'),
    fecha: document.getElementById('fecha'),
    fechaHint: document.getElementById('fechaHint'),
    hora: document.getElementById('hora'),
    horaError: document.getElementById('horaError'),
    submitBtn: document.getElementById('submitBtn'),
    form: document.getElementById('reservaForm')
};

/**
 * Habilita o deshabilita el botón de envío según la validación
 */
function actualizarEstadoBoton() {
    const esValido = estadoValidacion.fecha && estadoValidacion.hora;
    
    if (UI.submitBtn) {
        UI.submitBtn.disabled = !esValido;
        UI.submitBtn.style.opacity = esValido ? '1' : '0.6';
        UI.submitBtn.style.cursor = esValido ? 'pointer' : 'not-allowed';
    }
}

/**
 * Valida que la fecha no sea fin de semana (Sábado/Domingo)
 */
function validarFinDeSemana() {
    if (UI.fecha && UI.fecha.value) {
        const fecha = new Date(UI.fecha.value);
        const diaSemana = fecha.getDay(); // 0=Domingo, 6=Sábado

        if (diaSemana === 0 || diaSemana === 6) {
            UI.fechaHint.innerHTML = '<span style="color: #dc2626;">No se pueden realizar reservas los fines de semana</span>';
            estadoValidacion.fecha = false;
        } else {
            UI.fechaHint.innerHTML = 'Día válido';
            estadoValidacion.fecha = true;
        }
    } else {
        estadoValidacion.fecha = false;
    }
    actualizarEstadoBoton();
}

/**
 * Valida que la hora seleccionada esté en la lista permitida
 */
function validarHora() {
    if (UI.hora && UI.hora.value) {
        let valor = UI.hora.value.substring(0, 5); // Asegurar formato HH:MM

        if (HORAS_VALIDAS.includes(valor)) {
            UI.horaError.style.display = 'none';
            UI.horaError.textContent = '';
            estadoValidacion.hora = true;
        } else {
            UI.horaError.style.display = 'inline';
            UI.horaError.textContent = 'Hora no válida. Bloques de 90 min desde las 08:30.';
            estadoValidacion.hora = false;
        }
    } else {
        UI.horaError.style.display = 'inline';
        UI.horaError.textContent = 'Debe seleccionar una hora válida.';
        estadoValidacion.hora = false;
    }
    actualizarEstadoBoton();
}

/**
 * Calcula el precio dinámicamente basado en las reglas de negocio
 */
function calcularPrecio() {
    // Si no hay selección o el usuario es exento (Profesor/Admin/Edición), salir
    const config = window.ReservaConfig || {};
    if (!UI.espacio.value || config.esProfesor || config.esAdmin || config.esEdicion) {
        if (UI.precioInfo) UI.precioInfo.style.display = 'none';
        if (UI.btnText) UI.btnText.textContent = 'Continuar';
        return;
    }

    const opcion = UI.espacio.options[UI.espacio.selectedIndex];
    const texto = opcion.text.toLowerCase();
    const nombre = (opcion.getAttribute('data-nombre') || '').toLowerCase();
    const tipo = (opcion.getAttribute('data-tipo') || '').toLowerCase();
    const tieneTuo = UI.tuo ? UI.tuo.checked : false;

    let precio = 0;
    let descripcion = 'Instalación gratuita';

    // Lógica de precios
    if (texto.includes('pabellón') || texto.includes('pabellon')) {
        const conLuz = texto.includes('luz') || nombre.includes('luz');
        precio = tieneTuo 
            ? (conLuz ? PRECIOS.pabellon.conTuo.conLuz : PRECIOS.pabellon.conTuo.sinLuz)
            : (conLuz ? PRECIOS.pabellon.sinTuo.conLuz : PRECIOS.pabellon.sinTuo.sinLuz);
        descripcion = `Pabellón ${conLuz ? 'con luz' : 'sin luz'} - ${tieneTuo ? 'Con TUO' : 'Sin TUO'}`;
    
    } else if (texto.includes('aula') || tipo.includes('aula') || texto.includes('sala') || tipo.includes('sala')) {
        precio = tieneTuo ? 0 : 15.00;
        descripcion = tieneTuo ? 'Gratuita con TUO' : 'Tarifa Estándar';
    
    } else if (texto.includes('tenis') || tipo.includes('tenis') || texto.includes('pádel') || tipo.includes('pádel') || tipo.includes('padel')) {
        const conLuz = texto.includes('luz') || nombre.includes('luz');
        const categoria = tipo.includes('tenis') ? 'tenis' : 'padel';
        
        precio = tieneTuo
            ? (conLuz ? PRECIOS[categoria].conTuo.conLuz : PRECIOS[categoria].conTuo.sinLuz)
            : (conLuz ? PRECIOS[categoria].sinTuo.conLuz : PRECIOS[categoria].sinTuo.sinLuz);
        descripcion = `${tipo.charAt(0).toUpperCase() + tipo.slice(1)} ${conLuz ? 'con luz' : 'sin luz'} - ${tieneTuo ? 'Con TUO' : 'Sin TUO'}`;
    }

    // Actualizar UI
    if (UI.precioInfo) {
        UI.precioAmount.textContent = precio.toFixed(2) + ' €';
        UI.precioDesc.textContent = descripcion;
        UI.precioInfo.style.display = 'block';
    }

    if (UI.btnText) {
        UI.btnText.textContent = (precio === 0) ? 'Realizar Reserva Gratuita' : 'Continuar al Pago';
    }
}

// ==========================================
// INICIALIZACIÓN
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    
    // Listeners
    if (UI.espacio) UI.espacio.addEventListener('change', calcularPrecio);
    if (UI.tuo) UI.tuo.addEventListener('change', calcularPrecio);
    if (UI.fecha) UI.fecha.addEventListener('change', validarFinDeSemana);
    if (UI.hora) UI.hora.addEventListener('change', validarHora);

    // Validación al enviar
    if (UI.form) {
        UI.form.addEventListener('submit', (e) => {
            validarFinDeSemana();
            validarHora();
            if (!estadoValidacion.fecha || !estadoValidacion.hora) {
                e.preventDefault();
            }
        });
    }

    // Estado inicial
    if (UI.fecha && UI.fecha.value) validarFinDeSemana();
    else actualizarEstadoBoton(); // Inicia desactivado si vacío

    if (UI.hora && UI.hora.value) validarHora();
    else actualizarEstadoBoton();

    if (UI.espacio && UI.espacio.value) calcularPrecio();
});

// ==========================================
// INICIALIZACIÓN AL CARGAR DOM
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Asignar Listeners
    if (UI.espacio) UI.espacio.addEventListener('change', calcularPrecio);
    if (UI.tuo) UI.tuo.addEventListener('change', calcularPrecio);
    if (UI.fecha) UI.fecha.addEventListener('change', validarFinDeSemana);
    if (UI.hora) UI.hora.addEventListener('change', validarHora);

    if (UI.form) {
        UI.form.addEventListener('submit', (e) => {
            // Re-validar al enviar por si acaso
            validarFinDeSemana();
            validarHora();
            
            // Permitir envío si es válido, prevenir si no
            if (!estadoValidacion.fecha || !estadoValidacion.hora) {
                e.preventDefault();
                // Opcional: Mostrar un alert genérico
                // alert('Por favor, corrige los errores en el formulario.');
            }
        });
    }

    // 2. Ejecutar validaciones iniciales (Lee lo que el JSP pintó)
    
    // Validar fecha si ya tiene valor
    if (UI.fecha && UI.fecha.value) {
        validarFinDeSemana();
    } else {
        actualizarEstadoBoton(); // Deshabilita el botón al inicio si está vacío
    }

    // Validar hora si ya tiene valor
    if (UI.hora && UI.hora.value) {
        validarHora();
    }

    // Calcular precio si ya hay instalación seleccionada
    if (UI.espacio && UI.espacio.value) {
        calcularPrecio();
    }
});