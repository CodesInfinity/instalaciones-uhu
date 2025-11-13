/**
 * SCRIPT: FORMULARIO DE INSTALACIÓN
 *
 * Contiene toda la lógica del lado del cliente para el formulario de instalaciones:
 * - Contador de caracteres
 * - Preview de imagen
 * - Validaciones
 * - Interacciones con el usuario
 *
 * @author agustinrodriguez
 */

// Ejecutar cuando el documento esté completamente cargado
document.addEventListener("DOMContentLoaded", () => {
  initializeForm()
})

/**
 * FUNCIÓN: initializeForm
 * Inicializa todos los eventos y funcionalidades del formulario
 */
function initializeForm() {
  // Inicializar contador de caracteres
  initializeCharacterCounter()

  // Inicializar evento de validación
  initializeFormValidation()
}

/**
 * FUNCIÓN: initializeCharacterCounter
 * Configura el contador de caracteres para la descripción
 */
function initializeCharacterCounter() {
  const descripcion = document.getElementById("descripcion")
  const contador = document.getElementById("contadorCaracteres")

  if (descripcion && contador) {
    // Actualizar contador al escribir
    descripcion.addEventListener("input", function () {
      const length = this.value.length
      contador.textContent = length + "/500"

      // Cambiar color si se excede el límite
      if (length > 500) {
        contador.classList.add("text-danger")
      } else {
        contador.classList.remove("text-danger")
      }
    })

    // Inicializar valor del contador
    contador.textContent = descripcion.value.length + "/500"
  }
}

/**
 * FUNCIÓN: initializeFormValidation
 * Configura validaciones del formulario
 */
function initializeFormValidation() {
  const form = document.getElementById("formInstalacion")

  if (form) {
    form.addEventListener("submit", (e) => {
      // Validar campos obligatorios
      const nombre = document.getElementById("nombre").value.trim()
      const tipo = document.getElementById("tipo").value
      const ubicacion = document.getElementById("ubicacion").value.trim()
      const descripcion = document.getElementById("descripcion").value

      // Si falta algún campo requerido, prevenir envío
      if (!nombre || !tipo || !ubicacion) {
        e.preventDefault()
        showAlert("Por favor, complete todos los campos obligatorios (*)", "error")
        return false
      }

      // Si la descripción excede 500 caracteres, prevenir envío
      if (descripcion.length > 500) {
        e.preventDefault()
        showAlert("La descripción no puede exceder los 500 caracteres", "error")
        return false
      }
    })
  }
}

/**
 * FUNCIÓN: previewImage
 * Muestra una vista previa de la imagen seleccionada
 *
 * @param input - El elemento input file
 */
function previewImage(input) {
  const preview = document.getElementById("imagePreview")

  // Verificar que haya archivos seleccionados
  if (input.files && input.files[0]) {
    const reader = new FileReader()

    // Cuando la imagen se haya leído
    reader.onload = (e) => {
      // Buscar o crear elemento img
      let img = preview.querySelector("img")
      if (!img) {
        img = document.createElement("img")
        img.id = "previewImage"
        preview.innerHTML = ""
        preview.appendChild(img)
      }

      // Asignar la imagen leída como src
      img.src = e.target.result
      img.alt = "Vista previa"
    }

    // Leer el archivo como Data URL
    reader.readAsDataURL(input.files[0])
  }
}

/**
 * FUNCIÓN: showAlert
 * Muestra un mensaje al usuario
 *
 * @param message - Mensaje a mostrar
 * @param type - Tipo: 'error', 'success', 'info'
 */
function showAlert(message, type = "info") {
  const alertDiv = document.createElement("div")
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
    `
  alertDiv.textContent = message

  document.body.appendChild(alertDiv)

  // Remover el alert después de 3 segundos
  setTimeout(() => {
    if (document.body.contains(alertDiv)) {
      document.body.removeChild(alertDiv)
    }
  }, 3000)
}
