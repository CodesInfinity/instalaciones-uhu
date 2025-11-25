/* * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

/**
 * @fileoverview GESTIÓN DEL MODAL DE CIERRE DE SESIÓN
 * * Este script controla la lógica de interfaz (UI) para la ventana modal
 * de confirmación de logout.
 * * Funcionalidades:
 * - Apertura y cierre del modal.
 * - Bloqueo de scroll en el body para evitar desplazamiento trasero.
 * - Cierre accesible mediante tecla ESC y clic en el overlay.
 * * @author agustinrodriguez
 * @version 1.0.0
 */

/**
 * Activa la visualización del modal de confirmación.
 * Generalmente invocado directamente desde el atributo 'onclick' de un botón en el HTML.
 * * Efectos secundarios:
 * - Cambia el estilo display a 'flex'.
 * - Establece overflow: hidden en el body para bloquear el scroll de la página.
 */
function confirmarLogout() {
    const modal = document.getElementById('logoutModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden'; // UX: Congela el fondo
    }
}

/**
 * Oculta el modal de confirmación y restaura el estado normal de la página.
 * Puede ser llamado por el botón "Cancelar" o por eventos de cierre (ESC/Click fuera).
 * * Efectos secundarios:
 * - Cambia el estilo display a 'none'.
 * - Restaura el scroll del documento.
 */
function cerrarModalLogout() {
    const modal = document.getElementById('logoutModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = ''; // UX: Reactiva el scroll
    }
}

/**
 * Inicialización de listeners de eventos.
 * Configura comportamientos de accesibilidad y usabilidad una vez cargado el DOM.
 */
document.addEventListener('DOMContentLoaded', function() {
    
    const logoutModal = document.getElementById('logoutModal');

    // ---------------------------------------------------------
    // 1. CIERRE POR CLICK EN OVERLAY (FONDO OSCURO)
    // ---------------------------------------------------------
    if (logoutModal) {
        logoutModal.addEventListener('click', function(e) {
            // Verifica que el click sea exactamente en el contenedor padre (overlay)
            // y no en el contenido interno (la tarjeta del modal).
            if (e.target === this) {
                cerrarModalLogout();
            }
        });
    }

    // ---------------------------------------------------------
    // 2. CIERRE POR TECLADO (ACCESIBILIDAD)
    // ---------------------------------------------------------
    document.addEventListener('keydown', function(e) {
        // Detecta la pulsación de la tecla Escape (Esc)
        if (e.key === 'Escape') {
            cerrarModalLogout();
        }
    });

});