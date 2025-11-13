/**
 * SCRIPT: SISTEMA DE NAVEGACIÓN Y UTILIDADES
 *
 * Contiene la lógica del cliente para:
 * - Navegación responsive (mobile/desktop)
 * - Menú hamburguesa
 * - Dropdown de usuario
 * - Validaciones de formulario
 * - Mejoras de accesibilidad
 *
 * @author agustinrodriguez
 */

// Ejecutar cuando el DOM esté completamente cargado
document.addEventListener("DOMContentLoaded", () => {
  console.log("[v0] Inicializando sistema de navegación UHU...")

  // DETECCIÓN AUTOMÁTICA DEL TIPO DE NAVBAR
  const navToggle = document.getElementById("navToggle")
  const navMenuMobile = document.getElementById("navMenuMobile")
  const hamburger = document.getElementById("hamburger")
  const mobileMenu = document.getElementById("mobileMenu")

  // ===== SISTEMA PARA NAVBAR PROFESIONAL (layout.jsp) =====
  if (navToggle && navMenuMobile) {
    console.log("[v0] Inicializando navbar profesional...")
    initializeProfessionalNavbar(navToggle, navMenuMobile)
  }

  // ===== SISTEMA PARA NAVBAR ORIGINAL (index.html) =====
  else if (hamburger && mobileMenu) {
    console.log("[v0] Inicializando navbar original...")
    initializeOriginalNavbar(hamburger, mobileMenu)
  }

  // ===== FUNCIONALIDADES COMUNES =====
  initializeCommonFeatures(navToggle, navMenuMobile, hamburger, mobileMenu)
})

/**
 * FUNCIÓN: initializeProfessionalNavbar
 * Configura el comportamiento del navbar profesional (layout.jsp)
 */
function initializeProfessionalNavbar(navToggle, navMenuMobile) {
  // Toggle menú móvil al hacer click en hamburguesa
  navToggle.addEventListener("click", () => {
    navMenuMobile.classList.toggle("active")
    navToggle.classList.toggle("active")

    // Prevenir scroll del body
    document.body.style.overflow = navMenuMobile.classList.contains("active") ? "hidden" : ""
  })

  // Cerrar menú al hacer click en un enlace
  const mobileLinks = navMenuMobile.querySelectorAll("a")
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      navMenuMobile.classList.remove("active")
      navToggle.classList.remove("active")
      document.body.style.overflow = ""
    })
  })

  // Cerrar menú al hacer click fuera de él
  document.addEventListener("click", (event) => {
    if (
      navMenuMobile.classList.contains("active") &&
      !navToggle.contains(event.target) &&
      !navMenuMobile.contains(event.target)
    ) {
      navMenuMobile.classList.remove("active")
      navToggle.classList.remove("active")
      document.body.style.overflow = ""
    }
  })

  // Cerrar menú al redimensionar ventana
  window.addEventListener("resize", () => {
    if (window.innerWidth > 768) {
      navMenuMobile.classList.remove("active")
      navToggle.classList.remove("active")
      document.body.style.overflow = ""
    }
  })

  // Configurar dropdown de usuario
  const userProfile = document.querySelector(".user-profile")
  if (userProfile) {
    userProfile.addEventListener("click", function (e) {
      if (window.innerWidth <= 768) {
        this.classList.toggle("active")
      }
    })

    document.addEventListener("click", (event) => {
      if (!userProfile.contains(event.target)) {
        userProfile.classList.remove("active")
      }
    })
  }
}

/**
 * FUNCIÓN: initializeOriginalNavbar
 * Configura el comportamiento del navbar original (index.html)
 */
function initializeOriginalNavbar(hamburger, mobileMenu) {
  // Toggle menú móvil al hacer click en hamburguesa
  hamburger.addEventListener("click", () => {
    mobileMenu.classList.toggle("active")
    hamburger.classList.toggle("is-open")
    document.body.style.overflow = mobileMenu.classList.contains("active") ? "hidden" : ""
  })

  // Cerrar menú al hacer click en un enlace
  const mobileLinks = mobileMenu.querySelectorAll("a")
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      mobileMenu.classList.remove("active")
      hamburger.classList.remove("is-open")
      document.body.style.overflow = ""
    })
  })

  // Cerrar menú al hacer click fuera de él
  document.addEventListener("click", (event) => {
    if (
      mobileMenu.classList.contains("active") &&
      !hamburger.contains(event.target) &&
      !mobileMenu.contains(event.target)
    ) {
      mobileMenu.classList.remove("active")
      hamburger.classList.remove("is-open")
      document.body.style.overflow = ""
    }
  })

  // Cerrar menú al redimensionar ventana
  window.addEventListener("resize", () => {
    if (window.innerWidth > 768) {
      mobileMenu.classList.remove("active")
      hamburger.classList.remove("is-open")
      document.body.style.overflow = ""
    }
  })
}

/**
 * FUNCIÓN: initializeCommonFeatures
 * Funcionalidades comunes para ambos sistemas de navegación
 */
function initializeCommonFeatures(navToggle, navMenuMobile, hamburger, mobileMenu) {
  // Scroll suave para enlaces internos
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      const href = this.getAttribute("href")

      if (href !== "#" && href.length > 1) {
        e.preventDefault()
        const target = document.querySelector(href)
        if (target) {
          // Cerrar menús móviles
          if (navMenuMobile) navMenuMobile.classList.remove("active")
          if (navToggle) navToggle.classList.remove("active")
          if (mobileMenu) mobileMenu.classList.remove("active")
          if (hamburger) hamburger.classList.remove("is-open")
          document.body.style.overflow = ""

          target.scrollIntoView({ behavior: "smooth", block: "start" })
        }
      }
    })
  })

  // Efectos de hover en enlaces
  const navLinks = document.querySelectorAll(".nav-link, .btn-nav, .nav-menu a")
  navLinks.forEach((link) => {
    link.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-1px)"
    })

    link.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)"
    })
  })

  // Validación de formularios
  const forms = document.querySelectorAll("form")
  forms.forEach((form) => {
    form.addEventListener("submit", function (e) {
      const requiredFields = this.querySelectorAll("[required]")
      let isValid = true

      requiredFields.forEach((field) => {
        if (!field.value.trim()) {
          isValid = false
          field.style.borderColor = "#dc2626"

          setTimeout(() => {
            field.style.borderColor = ""
          }, 3000)
        } else {
          field.style.borderColor = ""
        }
      })

      if (!isValid) {
        e.preventDefault()
        showErrorMessage("Por favor, complete todos los campos requeridos.")
      }
    })
  })

  // Mejoras de accesibilidad
  const focusableElements = document.querySelectorAll("button, a, input, select, textarea")
  focusableElements.forEach((el) => {
    el.addEventListener("focus", function () {
      this.style.outline = "2px solid var(--color-uhu-red)"
      this.style.outlineOffset = "2px"
    })

    el.addEventListener("blur", function () {
      this.style.outline = ""
    })
  })

  // Animación de carga
  window.addEventListener("load", () => {
    document.body.style.opacity = "0"
    document.body.style.transition = "opacity 0.3s ease"

    setTimeout(() => {
      document.body.style.opacity = "1"
    }, 100)

    console.log("[v0] Sistema de navegación UHU cargado correctamente")
  })

  // Manejo de errores global
  window.addEventListener("error", (e) => {
    console.error("[v0] Error en el sistema de navegación:", e.error)
  })
}

/**
 * FUNCIÓN: showErrorMessage
 * Muestra un mensaje de error al usuario
 */
function showErrorMessage(message) {
  const errorMsg = document.createElement("div")
  errorMsg.textContent = message
  errorMsg.style.cssText =
    "position:fixed; top:20px; right:20px; background:#dc2626; color:white; padding:1rem; border-radius:0.5rem; z-index:10000;"
  document.body.appendChild(errorMsg)

  setTimeout(() => {
    if (document.body.contains(errorMsg)) {
      document.body.removeChild(errorMsg)
    }
  }, 3000)
}
