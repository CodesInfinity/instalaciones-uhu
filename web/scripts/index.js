/**
 * @fileoverview SISTEMA DE NAVEGACIÓN Y UTILIDADES UI
 * * Este script centraliza la lógica de interacción del cliente (Frontend).
 * Implementa un patrón de detección de características para soportar
 * dos arquitecturas de navegación distintas (Legacy vs Modern) en el mismo sitio.
 * * Funcionalidades principales:
 * - Gestión del estado del menú móvil (apertura/cierre/bloqueo de scroll).
 * - Navegación SPA simulada (smooth scroll).
 * - Validaciones visuales de formularios.
 * - Mejoras de accesibilidad (A11y) y focus management.
 * * @author agustinrodriguez
 * @version 1.1.0
 */

// Inicialización principal al cargar el DOM
document.addEventListener("DOMContentLoaded", () => {
  console.log("Inicializando sistema de navegación UHU...");

  // Referencias a elementos del DOM para la detección de características
  const navToggle = document.getElementById("navToggle");      // Botón menú (Layout Nuevo)
  const navMenuMobile = document.getElementById("navMenuMobile"); // Contenedor menú (Layout Nuevo)
  
  const hamburger = document.getElementById("hamburger");      // Botón menú (Layout Original)
  const mobileMenu = document.getElementById("mobileMenu");    // Contenedor menú (Layout Original)

  // 1. ESTRATEGIA DE CARGA: NAVBAR PROFESIONAL (layout.jsp)
  // Se activa si existen los IDs correspondientes a la nueva estructura.
  if (navToggle && navMenuMobile) {
    console.log("Detectado entorno: Navbar Profesional");
    initializeNavbar(navToggle, navMenuMobile);
  }

  // 2. ESTRATEGIA DE CARGA: NAVBAR ORIGINAL (index.html)
  // Se activa si existen los IDs de la estructura antigua.
  else if (hamburger && mobileMenu) {
    console.log("Detectado entorno: Navbar Original");
    initializeOriginalNavbar(hamburger, mobileMenu);
  }

  // 3. CARGA DE UTILIDADES TRANSVERSALES
  // Se ejecutan independientemente del tipo de navbar detectado.
  initializeCommonFeatures(navToggle, navMenuMobile, hamburger, mobileMenu);
});

/**
 * Inicializa la lógica para la barra de navegación "Profesional".
 * Gestiona la interacción del menú lateral y el perfil de usuario.
 * * @param {HTMLElement} navToggle - El botón/icono que activa el menú.
 * @param {HTMLElement} navMenuMobile - El contenedor del menú que se desliza/aparece.
 */
function initializeNavbar(navToggle, navMenuMobile) {
  // --- Evento: Abrir/Cerrar Menú ---
  navToggle.addEventListener("click", () => {
    navMenuMobile.classList.toggle("active");
    navToggle.classList.toggle("active");

    // UX: Bloquear el scroll del fondo cuando el menú está abierto
    document.body.style.overflow = navMenuMobile.classList.contains("active") ? "hidden" : "";
  });

  // --- Evento: Navegación Interna ---
  // Cierra el menú automáticamente al seleccionar una opción
  const mobileLinks = navMenuMobile.querySelectorAll("a");
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      navMenuMobile.classList.remove("active");
      navToggle.classList.remove("active");
      document.body.style.overflow = "";
    });
  });

  // --- Evento: Click Outside (UX) ---
  // Cierra el menú si el usuario toca cualquier zona fuera del menú
  document.addEventListener("click", (event) => {
    const isClickInside = navToggle.contains(event.target) || navMenuMobile.contains(event.target);
    
    if (navMenuMobile.classList.contains("active") && !isClickInside) {
      navMenuMobile.classList.remove("active");
      navToggle.classList.remove("active");
      document.body.style.overflow = "";
    }
  });

  // --- Evento: Responsive Reset ---
  // Asegura que el menú se resetee si se cambia la orientación del dispositivo o tamaño de ventana
  window.addEventListener("resize", () => {
    if (window.innerWidth > 768) {
      navMenuMobile.classList.remove("active");
      navToggle.classList.remove("active");
      document.body.style.overflow = "";
    }
  });

  // --- Lógica: Dropdown de Usuario ---
  const userProfile = document.querySelector(".user-profile");
  if (userProfile) {
    // En móvil, el hover no existe, convertimos la interacción a click
    userProfile.addEventListener("click", function (e) {
      if (window.innerWidth <= 768) {
        this.classList.toggle("active");
      }
    });

    // Cierra el dropdown de usuario al hacer click fuera
    document.addEventListener("click", (event) => {
      if (!userProfile.contains(event.target)) {
        userProfile.classList.remove("active");
      }
    });
  }
}

/**
 * Inicializa la lógica para la barra de navegación "Original/Legacy".
 * Utiliza clases CSS diferentes ('is-open' vs 'active') pero mantiene lógica similar.
 * * @param {HTMLElement} hamburger - El botón con icono de hamburguesa.
 * @param {HTMLElement} mobileMenu - El contenedor del menú desplegable.
 */
function initializeOriginalNavbar(hamburger, mobileMenu) {
  // --- Evento: Toggle Menú ---
  hamburger.addEventListener("click", () => {
    mobileMenu.classList.toggle("active");
    hamburger.classList.toggle("is-open"); // Nota: Usa 'is-open' específicamente para la animación del icono antiguo
    document.body.style.overflow = mobileMenu.classList.contains("active") ? "hidden" : "";
  });

  // --- Evento: Cerrar al navegar ---
  const mobileLinks = mobileMenu.querySelectorAll("a");
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      mobileMenu.classList.remove("active");
      hamburger.classList.remove("is-open");
      document.body.style.overflow = "";
    });
  });

  // --- Evento: Click Outside ---
  document.addEventListener("click", (event) => {
    const isClickInside = hamburger.contains(event.target) || mobileMenu.contains(event.target);

    if (mobileMenu.classList.contains("active") && !isClickInside) {
      mobileMenu.classList.remove("active");
      hamburger.classList.remove("is-open");
      document.body.style.overflow = "";
    }
  });

  // --- Evento: Reset en Resize ---
  window.addEventListener("resize", () => {
    if (window.innerWidth > 768) {
      mobileMenu.classList.remove("active");
      hamburger.classList.remove("is-open");
      document.body.style.overflow = "";
    }
  });
}

/**
 * Configura funcionalidades transversales que aplican a toda la aplicación,
 * independientemente del layout de navegación utilizado.
 * * @param {HTMLElement|null} navToggle - Referencia al botón del menú profesional (puede ser null).
 * @param {HTMLElement|null} navMenuMobile - Referencia al menú profesional (puede ser null).
 * @param {HTMLElement|null} hamburger - Referencia al botón del menú original (puede ser null).
 * @param {HTMLElement|null} mobileMenu - Referencia al menú original (puede ser null).
 */
function initializeCommonFeatures(navToggle, navMenuMobile, hamburger, mobileMenu) {
  
  // ---------------------------------------------------------
  // 1. SCROLL SUAVE (Smooth Scroll)
  // Intercepta clicks en anclas para animar el desplazamiento
  // ---------------------------------------------------------
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      const href = this.getAttribute("href");

      if (href !== "#" && href.length > 1) {
        e.preventDefault();
        const target = document.querySelector(href);
        if (target) {
          // Limpieza: Cerrar cualquier menú abierto antes de desplazarse
          if (navMenuMobile) navMenuMobile.classList.remove("active");
          if (navToggle) navToggle.classList.remove("active");
          if (mobileMenu) mobileMenu.classList.remove("active");
          if (hamburger) hamburger.classList.remove("is-open");
          document.body.style.overflow = "";

          target.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      }
    });
  });

  // ---------------------------------------------------------
  // 2. MICRO-INTERACCIONES (Hover Effects)
  // Añade feedback visual sutil mediante JS
  // ---------------------------------------------------------
  const navLinks = document.querySelectorAll(".nav-link, .btn-nav, .nav-menu a");
  navLinks.forEach((link) => {
    link.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-1px)";
    });
    link.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
    });
  });

  // ---------------------------------------------------------
  // 3. VALIDACIÓN DE FORMULARIOS (Client-side)
  // Validación genérica para todos los formularios del sitio
  // ---------------------------------------------------------
  const forms = document.querySelectorAll("form");
  forms.forEach((form) => {
    form.addEventListener("submit", function (e) {
      const requiredFields = this.querySelectorAll("[required]");
      let isValid = true;

      requiredFields.forEach((field) => {
        // Verifica si está vacío o solo contiene espacios
        if (!field.value.trim()) {
          isValid = false;
          field.style.borderColor = "#dc2626"; // Color de error (Rojo UHU)

          // Restaura el color original tras 3 segundos
          setTimeout(() => {
            field.style.borderColor = "";
          }, 3000);
        } else {
          field.style.borderColor = "";
        }
      });

      if (!isValid) {
        e.preventDefault(); // Detiene el envío
        showErrorMessage("Por favor, complete todos los campos requeridos.");
      }
    });
  });

  // ---------------------------------------------------------
  // 4. ACCESIBILIDAD (A11y)
  // Mejora la visibilidad del foco para navegación por teclado
  // ---------------------------------------------------------
  const focusableElements = document.querySelectorAll("button, a, input, select, textarea");
  focusableElements.forEach((el) => {
    el.addEventListener("focus", function () {
      this.style.outline = "2px solid var(--color-uhu-red)";
      this.style.outlineOffset = "2px";
    });

    el.addEventListener("blur", function () {
      this.style.outline = "";
    });
  });

  // ---------------------------------------------------------
  // 5. TRANSICIÓN DE ENTRADA (Page Fade-in)
  // ---------------------------------------------------------
  window.addEventListener("load", () => {
    document.body.style.opacity = "0";
    document.body.style.transition = "opacity 0.3s ease";

    setTimeout(() => {
      document.body.style.opacity = "1";
    }, 100);

    console.log("Sistema de navegación UHU cargado correctamente");
  });

  // ---------------------------------------------------------
  // 6. GESTIÓN DE ERRORES GLOBAL
  // ---------------------------------------------------------
  window.addEventListener("error", (e) => {
    console.error("Error detectado en el sistema de navegación:", e.error);
  });
}

/**
 * Muestra una notificación tipo "Toast" flotante en la pantalla.
 * Se utiliza para feedback de validaciones o errores del sistema.
 * * @param {string} message - El texto a mostrar en la notificación.
 */
function showErrorMessage(message) {
  const errorMsg = document.createElement("div");
  errorMsg.textContent = message;
  // Estilos in-line para asegurar visibilidad sin depender de CSS externo
  errorMsg.style.cssText =
    "position:fixed; top:20px; right:20px; background:#dc2626; color:white; padding:1rem; border-radius:0.5rem; z-index:10000; box-shadow: 0 4px 6px rgba(0,0,0,0.1);";
  
  document.body.appendChild(errorMsg);

  // Auto-eliminación del elemento DOM tras 3 segundos
  setTimeout(() => {
    if (document.body.contains(errorMsg)) {
      document.body.removeChild(errorMsg);
    }
  }, 3000);
}