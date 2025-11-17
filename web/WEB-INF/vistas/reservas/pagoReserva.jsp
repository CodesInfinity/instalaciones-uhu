<%-- 
    Document   : pagoReserva
    Created on : 13 nov 2025
    Author     : agustinrodriguez
    
    Vista profesional para el formulario de pago con Stripe
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="pago-container">
    <div class="pago-card">
        <!-- Header de pago -->
        <div class="pago-header">
            <div class="pago-logo-uhu">
                <img src="${pageContext.request.contextPath}/img/logoUHU._Horizontal_Color_Positivo.png" alt="Universidad de Huelva" />
            </div>
            <h1 class="pago-title">Pago Seguro</h1>
            <p class="pago-subtitle">Completa tu reserva de forma segura</p>
        </div>

        <!-- Resumen de la reserva -->
        <div class="pago-resumen">
            <div class="pago-resumen-header">
                <h2>Resumen de tu Reserva</h2>
            </div>
            <div class="pago-resumen-body">
                <div class="pago-resumen-item">
                    <div class="pago-resumen-icon">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                        </svg>
                    </div>
                    <div class="pago-resumen-content">
                        <span class="pago-resumen-label">Instalación</span>
                        <strong class="pago-resumen-value">${espacio.nombre}</strong>
                    </div>
                </div>

                <div class="pago-resumen-item">
                    <div class="pago-resumen-icon">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                    </div>
                    <div class="pago-resumen-content">
                        <span class="pago-resumen-label">Fecha</span>
                        <strong class="pago-resumen-value">${fecha}</strong>
                    </div>
                </div>

                <div class="pago-resumen-item">
                    <div class="pago-resumen-icon">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                    </div>
                    <div class="pago-resumen-content">
                        <span class="pago-resumen-label">Horario</span>
                        <strong class="pago-resumen-value">${horaInicio} - ${horaFin}</strong>
                    </div>
                </div>

                <div class="pago-resumen-total">
                    <span class="pago-total-label">Total a Pagar</span>
                    <span class="pago-total-amount">${precio} €</span>
                </div>
            </div>
        </div>

        <!-- Formulario de Pago con Stripe -->
        <div class="pago-stripe-section">
            <div class="pago-stripe-header">
                <div class="pago-stripe-badge">
                    <svg fill="currentColor" viewBox="0 0 60 25">
                        <path d="M59.64 14.28h-8.06c.19 1.93 1.6 2.55 3.2 2.55 1.64 0 2.96-.37 4.05-.95v3.32a8.33 8.33 0 0 1-4.56 1.1c-4.01 0-6.83-2.5-6.83-7.48 0-4.19 2.39-7.52 6.3-7.52 3.92 0 5.96 3.28 5.96 7.5 0 0.4-0.04 1.26-0.06 1.48zm-5.92-5.62c-1.03 0-2.17.73-2.17 2.58h4.25c0-1.85-1.07-2.58-2.08-2.58zM40.95 20.3c-1.44 0-2.32-0.6-2.9-1.04l-0.02 4.63-4.12.87V5.57h3.76l0.08 1.02a4.7 4.7 0 0 1 3.23-1.29c2.9 0 5.62 2.6 5.62 7.4 0 5.23-2.7 7.6-5.65 7.6zM40 8.95c-0.95 0-1.54.34-1.97.81l0.02 6.12c0.4 0.44 0.98 0.78 1.95 0.78 1.52 0 2.54-1.65 2.54-3.87 0-2.15-1.04-3.84-2.54-3.84zM28.24 5.57h4.13v14.44h-4.13V5.57zm0-4.7L32.37 0v3.36l-4.13.88V0.88zm-4.32 9.35v9.79H19.8V5.57h3.7l0.12 1.22c1-1.77 3.07-1.41 3.62-1.22v3.79c-0.52-0.17-2.29-0.43-3.32 0.86zm-8.55 4.72c0 2.43 2.6 1.68 3.12 1.46v3.36c-0.55 0.3-1.54 0.54-2.89 0.54a4.15 4.15 0 0 1-4.27-4.24l0.01-13.17 4.02-0.86v3.54h3.14V9.1h-3.13v5.85zm-4.91 0.7c0 2.97-2.31 4.66-5.73 4.66a11.2 11.2 0 0 1-4.46-0.93v-3.93c1.38 0.75 3.1 1.31 4.46 1.31 0.92 0 1.53-0.24 1.53-1C6.26 13.77 0 14.51 0 9.95 0 7.04 2.28 5.3 5.62 5.3c1.36 0 2.72 0.2 4.09 0.75v3.88a9.23 9.23 0 0 0-4.1-1.06c-0.86 0-1.44 0.25-1.44 0.93 0 1.85 6.29 0.97 6.29 5.88z" fill="#635BFF"/>
                    </svg>
                </div>
                <div class="pago-stripe-title">
                    <h3>Pago con tarjeta</h3>
                    <p>Tus datos están protegidos con encriptación SSL</p>
                </div>
            </div>

            <form id="payment-form" action="${pageContext.request.contextPath}/reservas/procesar-pago" method="post">
                <!-- Datos ocultos de la reserva -->
                <input type="hidden" name="espacioId" value="${espacioId}">
                <input type="hidden" name="fecha" value="${fecha}">
                <input type="hidden" name="hora" value="${hora}">
                <input type="hidden" name="tieneTuo" value="${tieneTuo}">
                <input type="hidden" name="precio" value="${precio}">
                <input type="hidden" id="payment-method-id" name="paymentMethodId">

                <!-- Elemento de tarjeta de Stripe -->
                <div class="pago-form-group">
                    <label for="card-element">Información de la tarjeta</label>
                    <div id="card-element" class="pago-card-element"></div>
                    <div id="card-errors" class="pago-card-errors" role="alert"></div>
                </div>

                <!-- Información de modo test -->
                <div class="pago-test-notice">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <div>
                        <strong>Modo de prueba - Tarjetas de test</strong>
                        <p>Visa: <code>4242 4242 4242 4242</code> | Mastercard: <code>5555 5555 5555 4444</code></p>
                        <p>Fecha: Cualquier fecha futura | CVC: Cualquier 3 dígitos</p>
                    </div>
                </div>

                <!-- Botones de acción -->
                <div class="pago-actions">
                    <a href="${pageContext.request.contextPath}/reservas/nueva" class="btn-pago-secondary">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
                        </svg>
                        Volver
                    </a>
                    <button type="submit" id="submit-button" class="btn-pago-primary">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                        </svg>
                        <span id="button-text">Pagar ${precio} €</span>
                        <span id="button-spinner" class="pago-spinner" style="display: none;"></span>
                    </button>
                </div>
            </form>
        </div>

        <!-- Garantía de seguridad -->
        <div class="pago-security">
            <div class="pago-security-item">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
                <span>Pago seguro SSL</span>
            </div>
            <div class="pago-security-item">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
                </svg>
                <span>Protección de datos</span>
            </div>
            <div class="pago-security-item">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
                <span>Procesado por Stripe</span>
            </div>
        </div>
    </div>
</div>

<!-- Stripe.js v3 -->
<script src="https://js.stripe.com/v3/"></script>

<script>
// Inicializar Stripe con la clave pública real
const stripe = Stripe('pk_test_51SQcrdE9UFOohwbhnLd3oeK3hqG7D0YBzz5SGtkPMeOJModDhmr20d4fB4LkgovuMIjGS0hhb5M78HSM3vgiHUSF00EZRqlm29');

// Crear elementos de Stripe
const elements = stripe.elements();

// Estilo personalizado para el campo de tarjeta
const style = {
    base: {
        color: '#1f2937',
        fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
        fontSmoothing: 'antialiased',
        fontSize: '16px',
        lineHeight: '24px',
        '::placeholder': {
            color: '#9ca3af'
        }
    },
    invalid: {
        color: '#dc2626',
        iconColor: '#dc2626'
    }
};

// Crear el elemento de tarjeta
const cardElement = elements.create('card', {
    style: style,
    hidePostalCode: true
});

// Montar el elemento en el DOM
cardElement.mount('#card-element');

// Manejar errores de validación en tiempo real
cardElement.on('change', function(event) {
    const displayError = document.getElementById('card-errors');
    if (event.error) {
        displayError.textContent = event.error.message;
    } else {
        displayError.textContent = '';
    }
});

// Manejar el envío del formulario
const form = document.getElementById('payment-form');
const submitButton = document.getElementById('submit-button');
const buttonText = document.getElementById('button-text');
const buttonSpinner = document.getElementById('button-spinner');

form.addEventListener('submit', async function(event) {
    event.preventDefault();
    
    // Deshabilitar botón para evitar múltiples envíos
    submitButton.disabled = true;
    buttonText.style.display = 'none';
    buttonSpinner.style.display = 'inline-block';
    
    try {
        // Crear método de pago con Stripe
        const {paymentMethod, error} = await stripe.createPaymentMethod({
            type: 'card',
            card: cardElement,
        });
        
        if (error) {
            // Mostrar error al usuario
            const errorElement = document.getElementById('card-errors');
            errorElement.textContent = error.message;
            
            // Reactivar botón
            submitButton.disabled = false;
            buttonText.style.display = 'inline';
            buttonSpinner.style.display = 'none';
        } else {
            // Pago exitoso, guardar el ID del método de pago y enviar formulario
            document.getElementById('payment-method-id').value = paymentMethod.id;
            form.submit();
        }
    } catch (err) {
        console.error('Error al procesar el pago:', err);
        alert('Error al procesar el pago. Por favor, inténtalo de nuevo.');
        
        // Reactivar botón
        submitButton.disabled = false;
        buttonText.style.display = 'inline';
        buttonSpinner.style.display = 'none';
    }
});
</script>