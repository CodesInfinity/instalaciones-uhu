/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando sistema de navegación UHU...');
    
    // DETECCIÓN AUTOMÁTICA DEL TIPO DE NAVBAR
    const navToggle = document.getElementById('navToggle'); // Navbar profesional
    const navMenuMobile = document.getElementById('navMenuMobile'); // Navbar profesional
    const hamburger = document.getElementById('hamburger'); // Navbar original (index.html)
    const mobileMenu = document.getElementById('mobileMenu'); // Navbar original (index.html)
    
    // ===== SISTEMA PARA NAVBAR PROFESIONAL (layout.jsp) =====
    if (navToggle && navMenuMobile) {
        console.log('Inicializando navbar profesional...');
        
        navToggle.addEventListener('click', function() {
            navMenuMobile.classList.toggle('active');
            navToggle.classList.toggle('active');
            
            // Prevenir scroll del body cuando el menú está abierto
            document.body.style.overflow = navMenuMobile.classList.contains('active') ? 'hidden' : '';
        });
        
        // Cerrar menú al hacer click en un enlace (mobile)
        const mobileLinks = navMenuMobile.querySelectorAll('a');
        mobileLinks.forEach(link => {
            link.addEventListener('click', function() {
                navMenuMobile.classList.remove('active');
                navToggle.classList.remove('active');
                document.body.style.overflow = ''; // Restaurar scroll
            });
        });
        
        // Cerrar menú al hacer click fuera de él
        document.addEventListener('click', function(event) {
            if (navMenuMobile.classList.contains('active') && 
                !navToggle.contains(event.target) && 
                !navMenuMobile.contains(event.target)) {
                navMenuMobile.classList.remove('active');
                navToggle.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
        
        // Cerrar menú al redimensionar la ventana (si se cambia a desktop)
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                navMenuMobile.classList.remove('active');
                navToggle.classList.remove('active');
                document.body.style.overflow = '';
            }
        });
        
        // User dropdown para desktop (solo en navbar profesional)
        const userProfile = document.querySelector('.user-profile');
        if (userProfile) {
            userProfile.addEventListener('click', function(e) {
                // Solo activar en mobile o si es un click específico
                if (window.innerWidth <= 768) {
                    this.classList.toggle('active');
                }
            });
            
            // Cerrar dropdown al hacer click fuera
            document.addEventListener('click', function(event) {
                if (!userProfile.contains(event.target)) {
                    userProfile.classList.remove('active');
                }
            });
        }
    }
    
    // ===== SISTEMA PARA NAVBAR ORIGINAL (index.html) =====
    else if (hamburger && mobileMenu) {
        console.log('Inicializando navbar original...');
        
        hamburger.addEventListener('click', function() {
            mobileMenu.classList.toggle('active');
            hamburger.classList.toggle('is-open');
            
            // Prevenir scroll del body cuando el menú está abierto
            document.body.style.overflow = mobileMenu.classList.contains('active') ? 'hidden' : '';
        });
        
        // Cerrar menú al hacer click en un enlace
        const mobileLinks = mobileMenu.querySelectorAll('a');
        mobileLinks.forEach(link => {
            link.addEventListener('click', function() {
                mobileMenu.classList.remove('active');
                hamburger.classList.remove('is-open');
                document.body.style.overflow = '';
            });
        });
        
        // Cerrar menú al hacer click fuera de él
        document.addEventListener('click', function(event) {
            if (mobileMenu.classList.contains('active') && 
                !hamburger.contains(event.target) && 
                !mobileMenu.contains(event.target)) {
                mobileMenu.classList.remove('active');
                hamburger.classList.remove('is-open');
                document.body.style.overflow = '';
            }
        });
        
        // Cerrar menú al redimensionar la ventana
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                mobileMenu.classList.remove('active');
                hamburger.classList.remove('is-open');
                document.body.style.overflow = '';
            }
        });
    }
    
    // ===== FUNCIONALIDADES COMUNES PARA AMBOS SISTEMAS =====
    
    // Scroll suave para enlaces internos (funciona en ambos)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            
            // Solo aplicar scroll suave para enlaces internos (no para #)
            if (href !== '#' && href.length > 1) {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    // Cerrar menús móviles si están abiertos
                    if (navMenuMobile) navMenuMobile.classList.remove('active');
                    if (navToggle) navToggle.classList.remove('active');
                    if (mobileMenu) mobileMenu.classList.remove('active');
                    if (hamburger) hamburger.classList.remove('is-open');
                    document.body.style.overflow = '';
                    
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
    
    // Efectos de hover mejorados para elementos interactivos
    const navLinks = document.querySelectorAll('.nav-link, .btn-nav, .nav-menu a');
    navLinks.forEach(link => {
        link.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-1px)';
        });
        
        link.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // Prevenir envío de formularios vacíos (opcional - solo en páginas con formularios)
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const requiredFields = this.querySelectorAll('[required]');
            let isValid = true;
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.style.borderColor = '#dc2626';
                    
                    // Remover el estilo después de un tiempo
                    setTimeout(() => {
                        field.style.borderColor = '';
                    }, 3000);
                } else {
                    field.style.borderColor = '';
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                // Mostrar mensaje de error suave
                const errorMsg = document.createElement('div');
                errorMsg.textContent = 'Por favor, complete todos los campos requeridos.';
                errorMsg.style.cssText = 'position:fixed; top:20px; right:20px; background:#dc2626; color:white; padding:1rem; border-radius:0.5rem; z-index:10000;';
                document.body.appendChild(errorMsg);
                
                setTimeout(() => {
                    document.body.removeChild(errorMsg);
                }, 3000);
            }
        });
    });
    
    // Mejoras de accesibilidad - focus visible
    const focusableElements = document.querySelectorAll('button, a, input, select, textarea');
    focusableElements.forEach(el => {
        el.addEventListener('focus', function() {
            this.style.outline = '2px solid var(--color-uhu-red)';
            this.style.outlineOffset = '2px';
        });
        
        el.addEventListener('blur', function() {
            this.style.outline = '';
        });
    });
    
    // Animación de carga suave para la página
    window.addEventListener('load', function() {
        document.body.style.opacity = '0';
        document.body.style.transition = 'opacity 0.3s ease';
        
        setTimeout(function() {
            document.body.style.opacity = '1';
        }, 100);
        
        console.log('Sistema de navegación UHU cargado correctamente');
    });
    
    // Manejo de errores global
    window.addEventListener('error', function(e) {
        console.error('Error en el sistema de navegación:', e.error);
    });
});