/**
 * @fileoverview SISTEMA DE VALIDACIÓN DE FORMULARIOS CON FETCH API
 * * Este script implementa una capa robusta de validación cliente-servidor.
 * * Características principales:
 * - Validaciones síncronas (Regex, longitud, coincidencia de contraseñas).
 * - Validaciones asíncronas (Consultas al backend para Email/DNI únicos).
 * - Patrón 'Debounce' para optimizar llamadas al servidor en tiempo real.
 * - Gestión visual de errores (Input highlighting y mensajes contextuales).
 * - Detección automática del contexto (Registro vs Edición).
 * * @author agustinrodriguez
 * @version 2.0.0
 */

// ==========================================
// 1. UTILIDADES DE INTERFAZ DE USUARIO (UI)
// ==========================================

/**
 * Elimina los estilos de error/éxito y los mensajes de validación asociados a un campo.
 * Busca el contenedor padre para limpiar cualquier alerta visual previa.
 * * @param {HTMLElement} campo - El input del formulario a limpiar.
 */
function limpiarErrorCampo(campo) {
    campo.classList.remove('input-error');
    campo.classList.remove('input-success');
    
    // Busca el contenedor más cercano para encontrar el mensaje de error
    const formGroup = campo.closest('.form-group') || campo.closest('.input-group') || campo.parentElement;
    const todosLosErrores = formGroup.querySelectorAll('.campo-error');
    todosLosErrores.forEach(error => error.remove());
}

/**
 * Aplica estilos de error al campo e inyecta un mensaje descriptivo en el DOM.
 * * @param {HTMLElement} campo - El input que falló la validación.
 * @param {string} mensaje - El texto explicativo del error.
 */
function mostrarErrorCampo(campo, mensaje) {
    limpiarErrorCampo(campo); // Limpieza preventiva
    campo.classList.add('input-error');
    campo.classList.remove('input-success');
    
    // Creación dinámica del elemento de mensaje
    const errorDiv = document.createElement('div');
    errorDiv.className = 'campo-error';
    errorDiv.textContent = mensaje;
    
    const parent = campo.closest('.input-wrapper') || campo.parentElement;
    parent.parentElement.insertBefore(errorDiv, parent.nextSibling);
}

/**
 * Aplica estilos visuales de éxito (borde verde) cuando un campo es válido.
 * * @param {HTMLElement} campo - El input validado correctamente.
 */
function mostrarExitoCampo(campo) {
    limpiarErrorCampo(campo);
    campo.classList.add('input-success');
    campo.classList.remove('input-error');
}

/**
 * Determina si el formulario actual es de edición o creación.
 * Lo hace verificando la existencia y valor de un campo oculto 'id'.
 * * @param {HTMLFormElement} form - El formulario a inspeccionar.
 * @returns {boolean} - True si es edición (ID existe), False si es registro nuevo.
 */
function esFormularioEdicion(form) {
    const idField = form.querySelector('input[name="id"]');
    return idField && idField.value && idField.value.length > 0;
}

// ==========================================
// 2. VALIDACIONES SÍNCRONAS (CLIENT-SIDE)
// ==========================================

/**
 * Valida únicamente el formato del email mediante Regex.
 * * Nota: No verifica existencia en base de datos (usado en modo edición).
 * * @param {HTMLInputElement} campoEmail - Input del email.
 */
function validarEmailFormato(campoEmail) {
    campoEmail.addEventListener('input', function() {
        const email = this.value.trim();
        
        if (email.length === 0) {
            limpiarErrorCampo(this);
            return;
        }
        
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            mostrarErrorCampo(this, 'Formato de email inválido');
        } else {
            mostrarExitoCampo(this);
        }
    });
}

/**
 * Valida únicamente el formato del DNI (8 números + 1 letra).
 * * @param {HTMLInputElement} campoDni - Input del DNI.
 */
function validarDniFormato(campoDni) {
    campoDni.addEventListener('input', function() {
        const dni = this.value.trim();
        
        if (dni.length === 0) {
            limpiarErrorCampo(this);
            return;
        }
        
        const dniRegex = /^[0-9]{8}[A-Z]$/i;
        if (!dniRegex.test(dni)) {
            mostrarErrorCampo(this, 'Formato de DNI inválido (8 números + letra)');
        } else {
            mostrarExitoCampo(this);
        }
    });
}

/**
 * Valida la longitud mínima de la contraseña.
 * * @param {HTMLInputElement} campoPassword - Input de contraseña.
 */
function validarPassword(campoPassword) {
    campoPassword.addEventListener('input', function() {
        const password = this.value;
        
        // Lógica condicional: Si está vacío en edición, puede ser válido (no se cambia)
        if (password.length > 0 && password.length < 6) {
            mostrarErrorCampo(this, 'La contraseña debe tener al menos 6 caracteres');
        } else if (password.length >= 6) {
            mostrarExitoCampo(this);
        } else {
            limpiarErrorCampo(this);
        }
    });
}

/**
 * Compara dos campos de contraseña para asegurar que coinciden.
 * * @param {HTMLInputElement} campoPassword - Campo original.
 * @param {HTMLInputElement} campoConfirm - Campo de confirmación.
 */
function validarConfirmPassword(campoPassword, campoConfirm) {
    campoConfirm.addEventListener('input', function() {
        if (this.value.length === 0) {
            limpiarErrorCampo(this);
            return;
        }
        
        if (this.value !== campoPassword.value) {
            mostrarErrorCampo(this, 'Las contraseñas no coinciden');
        } else {
            mostrarExitoCampo(this);
        }
    });
    
    // Listener inverso: Si cambio la original, re-validar la confirmación
    campoPassword.addEventListener('input', function() {
        if (campoConfirm.value.length > 0) {
            if (campoConfirm.value !== this.value) {
                mostrarErrorCampo(campoConfirm, 'Las contraseñas no coinciden');
            } else {
                mostrarExitoCampo(campoConfirm);
            }
        }
    });
}

/**
 * Itera sobre todos los campos requeridos para asegurar que no estén vacíos.
 * * @param {HTMLFormElement} form - El formulario a validar.
 * @returns {boolean} - True si todos los campos requeridos tienen valor.
 */
function validarCamposObligatorios(form) {
    let esValido = true;
    const campos = form.querySelectorAll('input[required], select[required]');
    
    for (const campo of campos) {
        if (!campo.value.trim()) {
            mostrarErrorCampo(campo, 'Este campo es obligatorio');
            esValido = false;
        } else {
            limpiarErrorCampo(campo);
        }
    }
    return esValido;
}

// ==========================================
// 3. VALIDACIONES ASÍNCRONAS (SERVER-SIDE)
// ==========================================

/**
 * Utilidad para obtener la ruta base de la aplicación (Context Path).
 * Necesario para construir URLs relativas correctas para el Fetch.
 */
function getContextPath() {
    const path = window.location.pathname;
    const contextPath = path.substring(0, path.indexOf('/', 1));
    return contextPath || '';
}

/**
 * Consulta al servidor si un email está disponible.
 * * @async
 * @param {string} email - Email a verificar.
 * @param {string|null} userId - ID del usuario (para excluirse a sí mismo en ediciones).
 * @returns {Promise<Object>} - JSON con { valido: boolean, mensaje: string }.
 */
async function validarEmailBackend(email, userId = null) {
    try {
        let url = `${getContextPath()}/usuario/validar-email?email=${encodeURIComponent(email)}&ajax=true`;
        if (userId) {
            url += `&id=${userId}`;
        }
        
        const response = await fetch(url);
        
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('Respuesta no es JSON');
            return { valido: false, mensaje: 'Error del servidor' };
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error al validar email:', error);
        return { valido: false, mensaje: 'Error de conexión' };
    }
}

/**
 * Consulta al servidor si un DNI está disponible.
 * * @async
 * @param {string} dni - DNI a verificar.
 * @param {string|null} userId - ID opcional.
 * @returns {Promise<Object>} - Respuesta del servidor.
 */
async function validarDniBackend(dni, userId = null) {
    try {
        let url = `${getContextPath()}/usuario/validar-dni?dni=${encodeURIComponent(dni)}&ajax=true`;
        if (userId) {
            url += `&id=${userId}`;
        }
        
        const response = await fetch(url);
        
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('Respuesta no es JSON');
            return { valido: false, mensaje: 'Error del servidor' };
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error al validar DNI:', error);
        return { valido: false, mensaje: 'Error de conexión' };
    }
}

// ==========================================
// 4. LÓGICA DE TIEMPO REAL (DEBOUNCE)
// ==========================================



/**
 * Implementa validación de email en tiempo real con patrón DEBOUNCE.
 * * Flujo:
 * 1. Espera a que el usuario deje de escribir (500ms).
 * 2. Valida formato localmente (Regex).
 * 3. Si el formato es correcto, consulta al backend mediante Fetch.
 * * @param {HTMLInputElement} campoEmail - Elemento a vigilar.
 */
function validarEmailTiempoReal(campoEmail) {
    let timeoutId;
    
    campoEmail.addEventListener('input', function() {
        clearTimeout(timeoutId); // Reinicia el temporizador
        const email = this.value.trim();
        
        if (email.length === 0) {
            limpiarErrorCampo(this);
            return;
        }
        
        // Validación síncrona primero
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            mostrarErrorCampo(this, 'Formato de email inválido');
            return;
        }
        
        // Obtener contexto para la llamada ajax
        const form = this.closest('form');
        const userIdInput = form ? form.querySelector('input[name="id"]') : null;
        const userId = userIdInput ? userIdInput.value : null;
        
        // Debounce: Llamada asíncrona tras 500ms de inactividad
        timeoutId = setTimeout(async () => {
            const resultado = await validarEmailBackend(email, userId);
            if (!resultado.valido) {
                mostrarErrorCampo(campoEmail, resultado.mensaje || 'Email no disponible');
            } else {
                mostrarExitoCampo(campoEmail);
            }
        }, 500);
    });
}

/**
 * Implementa validación de DNI en tiempo real con patrón DEBOUNCE.
 * @param {HTMLInputElement} campoDni - Elemento a vigilar.
 */
function validarDniTiempoReal(campoDni) {
    let timeoutId;
    
    campoDni.addEventListener('input', function() {
        clearTimeout(timeoutId);
        const dni = this.value.trim();
        
        if (dni.length === 0) {
            limpiarErrorCampo(this);
            return;
        }
        
        const dniRegex = /^[0-9]{8}[A-Z]$/i;
        if (!dniRegex.test(dni)) {
            mostrarErrorCampo(this, 'Formato de DNI inválido (8 números + letra)');
            return;
        }
        
        const form = this.closest('form');
        const userIdInput = form ? form.querySelector('input[name="id"]') : null;
        const userId = userIdInput ? userIdInput.value : null;
        
        timeoutId = setTimeout(async () => {
            const resultado = await validarDniBackend(dni, userId);
            if (!resultado.valido) {
                mostrarErrorCampo(campoDni, resultado.mensaje || 'DNI no disponible');
            } else {
                mostrarExitoCampo(campoDni);
            }
        }, 500);
    });
}

// ==========================================
// 5. ORQUESTACIÓN Y EVENTOS PRINCIPALES
// ==========================================

/**
 * Realiza una validación exhaustiva de todo el formulario antes del envío.
 * Combina validaciones síncronas y asíncronas (esperando las promesas).
 * * @async
 * @param {HTMLFormElement} form - Formulario a validar.
 * @returns {Promise<boolean>} - True si todo es válido y se puede enviar.
 */
async function validarFormularioCompleto(form) {
    let esValido = true;
    const campos = form.querySelectorAll('input[required], select[required], textarea[required]');
    
    // 1. Campos vacíos
    for (const campo of campos) {
        if (!campo.value.trim()) {
            mostrarErrorCampo(campo, 'Este campo es obligatorio');
            esValido = false;
        }
    }
    
    // 2. Email (Regex + Backend)
    const emailField = form.querySelector('input[type="email"]');
    if (emailField && emailField.value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailField.value)) {
            mostrarErrorCampo(emailField, 'Formato de email inválido');
            esValido = false;
        } else {
            const resultado = await validarEmailBackend(emailField.value);
            if (!resultado.valido) {
                mostrarErrorCampo(emailField, resultado.mensaje);
                esValido = false;
            }
        }
    }
    
    // 3. DNI (Regex + Backend)
    const dniField = form.querySelector('input[name="dni"]');
    if (dniField && dniField.value) {
        const dniRegex = /^[0-9]{8}[A-Z]$/i;
        if (!dniRegex.test(dniField.value)) {
            mostrarErrorCampo(dniField, 'Formato de DNI inválido (8 números + letra)');
            esValido = false;
        } else {
            const resultado = await validarDniBackend(dniField.value);
            if (!resultado.valido) {
                mostrarErrorCampo(dniField, resultado.mensaje);
                esValido = false;
            }
        }
    }
    
    return esValido;
}

/**
 * INICIALIZACIÓN GLOBAL
 * Detecta qué tipo de formulario hay en la página (Registro, Login, Reserva)
 * y aplica las estrategias de validación correspondientes.
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Configurando validación de formularios');
    
    // Selectores para identificar el contexto de la página
    const formRegistro = document.querySelector('form[action*="save"], form[action*="registro"]');
    const formLogin = document.querySelector('form[action*="login"]');
    const formReserva = document.querySelector('form[action*="reservas"]');
    
    // --- LÓGICA PARA REGISTRO/EDICIÓN DE USUARIOS ---
    if (formRegistro) {
        const esEdicion = esFormularioEdicion(formRegistro);
        console.log('Formulario de ' + (esEdicion ? 'EDICIÓN' : 'REGISTRO NUEVO'));
        
        const emailField = formRegistro.querySelector('input[type="email"]');
        const dniField = formRegistro.querySelector('input[name="dni"]');
        const passwordField = formRegistro.querySelector('input[type="password"]');
        const confirmPasswordField = formRegistro.querySelector('input[name="confirmPassword"]');
        
        if (esEdicion) {
            // ESTRATEGIA EDICIÓN:
            // Validación relajada. Se asume que los datos actuales ya son válidos en backend.
            // Solo se validan formatos si el usuario los cambia.
            console.log('Modo edición - validando formatos (sin duplicados)');
            
            if (emailField) validarEmailFormato(emailField);
            if (dniField) validarDniFormato(dniField);
            if (passwordField) validarPassword(passwordField);
            
            formRegistro.addEventListener('submit', function(e) {
                e.preventDefault();
                
                let esValido = true;
                
                // Validaciones síncronas secuenciales
                esValido = validarCamposObligatorios(formRegistro);
                
                if (emailField && emailField.value) {
                    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                    if (!emailRegex.test(emailField.value.trim())) {
                        mostrarErrorCampo(emailField, 'Formato de email inválido');
                        esValido = false;
                    }
                }
                
                if (dniField && dniField.value) {
                    const dniRegex = /^[0-9]{8}[A-Z]$/i;
                    if (!dniRegex.test(dniField.value.trim())) {
                        mostrarErrorCampo(dniField, 'Formato de DNI inválido (8 números + letra)');
                        esValido = false;
                    }
                }
                
                if (passwordField && passwordField.value) {
                    if (passwordField.value.length < 6) {
                        mostrarErrorCampo(passwordField, 'La contraseña debe tener al menos 6 caracteres');
                        esValido = false;
                    }
                }
                
                if (esValido) {
                    formRegistro.submit();
                } else {
                    // UX: Auto-scroll al primer error
                    const primerError = formRegistro.querySelector('.input-error');
                    if (primerError) {
                        primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
            });
            
        } else {
            // ESTRATEGIA REGISTRO NUEVO:
            // Validación estricta. Chequeo en tiempo real contra base de datos.
            console.log('Modo registro nuevo - validación completa');
            
            if (emailField) validarEmailTiempoReal(emailField);
            if (dniField) validarDniTiempoReal(dniField);
            if (passwordField) validarPassword(passwordField);
            if (confirmPasswordField) validarConfirmPassword(passwordField, confirmPasswordField);
            
            formRegistro.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                // Esperar a que terminen todas las validaciones asíncronas
                const esValido = await validarFormularioCompleto(this);
                if (esValido) {
                    this.submit();
                } else {
                    const primerError = this.querySelector('.input-error');
                    if (primerError) {
                        primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
            });
        }
    }
    
    // --- LÓGICA PARA LOGIN ---
    if (formLogin) {
        console.log('Configurando validación para formulario de login');
        
        const emailField = formLogin.querySelector('input[type="email"]');
        const passwordField = formLogin.querySelector('input[type="password"]');
        
        // Validación "lazy" (solo al perder el foco/blur)
        if (emailField) {
            emailField.addEventListener('blur', function() {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                if (this.value && !emailRegex.test(this.value)) {
                    mostrarErrorCampo(this, 'Formato de email inválido');
                } else if (this.value) {
                    limpiarErrorCampo(this);
                }
            });
        }
        
        if (passwordField) {
            passwordField.addEventListener('blur', function() {
                if (this.value && this.value.length < 6) {
                    mostrarErrorCampo(this, 'La contraseña debe tener al menos 6 caracteres');
                } else if (this.value) {
                    limpiarErrorCampo(this);
                }
            });
        }
    }
    
    // --- LÓGICA PARA RESERVAS ---
    if (formReserva) {
        console.log('Configurando validación para formulario de reserva');
        
        formReserva.addEventListener('submit', function(e) {
            const campos = this.querySelectorAll('input[required], select[required]');
            let esValido = true;
            
            campos.forEach(campo => {
                if (!campo.value) {
                    mostrarErrorCampo(campo, 'Este campo es obligatorio');
                    esValido = false;
                } else {
                    limpiarErrorCampo(campo);
                }
            });
            
            if (!esValido) {
                e.preventDefault();
                const primerError = this.querySelector('.input-error');
                if (primerError) {
                    primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }
});

// ==========================================
// 6. INYECCIÓN DE ESTILOS CSS
// ==========================================

// Asegura que los estilos de error existan sin necesidad de un CSS externo
if (!document.getElementById('validacion-styles')) {
    const style = document.createElement('style');
    style.id = 'validacion-styles';
    style.textContent = `
        .input-error {
            border-color: #dc2626 !important;
            box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1) !important;
        }
        
        .input-success {
            border-color: #10b981 !important;
        }
        
        .input-success:focus {
            box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
        }
        
        .campo-error {
            color: #dc2626;
            font-size: 13px;
            margin-top: 6px;
            display: flex;
            align-items: center;
            gap: 6px;
            animation: slideDown 0.2s ease-out;
            padding: 8px 12px;
            background: #fef2f2;
            border-radius: 6px;
            border: 1px solid #fecaca;
        }
        
        .campo-error::before {
            font-size: 14px;
            flex-shrink: 0;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-5px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .form-group {
            margin-bottom: 24px;
        }
    `;
    document.head.appendChild(style);
}