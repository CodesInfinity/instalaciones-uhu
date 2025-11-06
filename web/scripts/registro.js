/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

// scripts/registro.js
document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM
    const solicitarProfesorCheckbox = document.getElementById('solicitarProfesor');
    const infoSolicitud = document.getElementById('infoSolicitud');

    // Mostrar/ocultar información de solicitud
    if (solicitarProfesorCheckbox && infoSolicitud) {
        solicitarProfesorCheckbox.addEventListener('change', function() {
            if (this.checked) {
                infoSolicitud.style.display = 'block';
            } else {
                infoSolicitud.style.display = 'none';
            }
        });

        // Inicializar estado al cargar la página
        if (solicitarProfesorCheckbox.checked) {
            infoSolicitud.style.display = 'block';
        }
    }
});