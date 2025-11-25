/* * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

/**
 * @fileoverview LÓGICA DEL FORMULARIO DE REGISTRO
 * * Este script maneja la interactividad del formulario de registro de usuarios.
 * * Funcionalidades:
 * - Control de visibilidad condicional para campos opcionales.
 * - UX: Muestra información específica solo cuando se solicita el rol de profesor.
 * * @author agustinrodriguez
 * @version 1.0.0
 */

// Scripts/registro.js

/**
 * Inicialización de eventos al cargar el DOM.
 */
document.addEventListener('DOMContentLoaded', function() {
    
    // ---------------------------------------------------------
    // 1. REFERENCIAS AL DOM
    // Captura de elementos necesarios para la lógica de visualización
    // ---------------------------------------------------------
    const solicitarProfesorCheckbox = document.getElementById('solicitarProfesor');
    const infoSolicitud = document.getElementById('infoSolicitud');

    // ---------------------------------------------------------
    // 2. LÓGICA DE VISIBILIDAD (TOGGLE)
    // Solo se ejecuta si ambos elementos existen en la página actual
    // ---------------------------------------------------------
    if (solicitarProfesorCheckbox && infoSolicitud) {
        
        /**
         * Event listener para detectar cambios en el checkbox.
         * Muestra u oculta el bloque de información adicional dinámicamente.
         */
        solicitarProfesorCheckbox.addEventListener('change', function() {
            if (this.checked) {
                // Mostrar instrucciones si el usuario quiere ser profesor
                infoSolicitud.style.display = 'block';
            } else {
                // Ocultar si se desmarca la opción
                infoSolicitud.style.display = 'none';
            }
        });

        // ---------------------------------------------------------
        // 3. ESTADO INICIAL
        // Verifica el estado del checkbox al cargar la página.
        // Es vital si el navegador recuerda la selección tras una recarga (caché)
        // o si el servidor devolvió el formulario con errores y el campo marcado.
        // ---------------------------------------------------------
        if (solicitarProfesorCheckbox.checked) {
            infoSolicitud.style.display = 'block';
        }
    }
});