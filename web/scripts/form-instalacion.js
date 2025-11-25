/**
 * @fileoverview LÓGICA DEL FORMULARIO DE INSTALACIÓN
 * * Este script gestiona la interacción del lado del cliente para el formulario
 * de creación y edición de instalaciones.
 * * Funcionalidades principales:
 * - Feedback visual en tiempo real (contador de caracteres).
 * - Previsualización de imágenes antes de la subida (FileReader API).
 * - Validación estricta de campos requeridos antes del envío.
 * - Sistema de notificaciones tipo "Toast" para errores y avisos.
 * * @author agustinrodriguez
 * @version 1.0.0
 */

// Inicialización del script cuando el DOM está listo
document.addEventListener("DOMContentLoaded", () => {
  initializeForm();
});

/**
 * Función orquestadora principal.
 * Inicializa todos los subsistemas del formulario (contadores y validaciones).
 */
function initializeForm() {
  // Inicializar contador de caracteres para el textarea
  initializeCharacterCounter();

  // Configurar los listeners para la validación del envío
  initializeFormValidation();
}

/**
 * Configura la lógica de conteo de caracteres para el campo de descripción.
 * Proporciona feedback visual en tiempo real al usuario.
 * * Comportamiento:
 * - Actualiza el contador formato "X/500".
 * - Añade la clase 'text-danger' si se supera el límite permitido.
 */
function initializeCharacterCounter() {
  const descripcion = document.getElementById("descripcion");
  const contador = document.getElementById("contadorCaracteres");

  if (descripcion && contador) {
    // Evento input: Se dispara cada vez que el usuario escribe o borra
    descripcion.addEventListener("input", function () {
      const length = this.value.length;
      contador.textContent = length + "/500";

      // Validación visual: Cambiar color si excede el límite
      if (length > 500) {
        contador.classList.add("text-danger");
      } else {
        contador.classList.remove("text-danger");
      }
    });

    // Inicialización: Establecer valor inicial al cargar la página (para ediciones)
    contador.textContent = descripcion.value.length + "/500";
  }
}

/**
 * Establece las reglas de validación que bloquean el envío del formulario.
 * Previene el comportamiento por defecto (submit) si no se cumplen las condiciones.
 */
function initializeFormValidation() {
  const form = document.getElementById("formInstalacion");

  if (form) {
    form.addEventListener("submit", (e) => {
      // Captura y limpieza de valores
      const nombre = document.getElementById("nombre").value.trim();
      const tipo = document.getElementById("tipo").value;
      const ubicacion = document.getElementById("ubicacion").value.trim();
      const descripcion = document.getElementById("descripcion").value;

      // Regla 1: Validar campos obligatorios
      if (!nombre || !tipo || !ubicacion) {
        e.preventDefault(); // Detiene el envío
        showAlert("Por favor, complete todos los campos obligatorios (*)", "error");
        return false;
      }

      // Regla 2: Validar longitud máxima de la descripción
      if (descripcion.length > 500) {
        e.preventDefault(); // Detiene el envío
        showAlert("La descripción no puede exceder los 500 caracteres", "error");
        return false;
      }
    });
  }
}

/**
 * Genera una vista previa de la imagen seleccionada por el usuario
 * utilizando la API FileReader, evitando la necesidad de subirla al servidor primero.
 * * Se invoca desde el evento 'onchange' del input file en el HTML.
 * * @param {HTMLInputElement} input - El elemento <input type="file"> que disparó el evento.
 */
function previewImage(input) {
  const preview = document.getElementById("imagePreview");

  // Verificar que el usuario haya seleccionado al menos un archivo
  if (input.files && input.files[0]) {
    const reader = new FileReader();

    // Callback: Se ejecuta cuando la lectura del archivo termina con éxito
    reader.onload = (e) => {
      // Buscar imagen existente o crear una nueva si es la primera vez
      let img = preview.querySelector("img");
      if (!img) {
        img = document.createElement("img");
        img.id = "previewImage";
        preview.innerHTML = ""; // Limpiar cualquier texto o placeholder previo
        preview.appendChild(img);
      }

      // Asignar el resultado (base64) al src de la imagen
      img.src = e.target.result;
      img.alt = "Vista previa";
    };

    // Iniciar la lectura del archivo como Data URL
    reader.readAsDataURL(input.files[0]);
  }
}

/**
 * Muestra una notificación flotante (Toast) en la esquina superior derecha.
 * Útil para feedback de éxito o error sin bloquear la interfaz.
 * * @param {string} message - El texto a mostrar en la alerta.
 * @param {('error'|'success'|'info')} [type="info"] - El tipo de alerta, determina el color de fondo.
 */
function showAlert(message, type = "info") {
  const alertDiv = document.createElement("div");
  
  // Estilos in-line para garantizar la visualización independiente del CSS externo
  alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === "error" ? "#dc2626" : type === "success" ? "#22c55e" : "#3b82f6"};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        z-index: 10000;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        font-weight: 600;
    `;
  alertDiv.textContent = message;

  document.body.appendChild(alertDiv);

  // Auto-destrucción del elemento después de 3 segundos
  setTimeout(() => {
    if (document.body.contains(alertDiv)) {
      document.body.removeChild(alertDiv);
    }
  }, 3000);
}