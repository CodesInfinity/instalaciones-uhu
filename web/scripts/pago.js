/**
 * SCRIPT: INTEGRACIÓN DE PAGOS CON STRIPE
 * Gestiona el ciclo de vida del elemento de tarjeta (Elements)
 * y el proceso de confirmación del pago (PaymentMethod).
 * * Requiere: Stripe.js v3 cargado previamente.
 * * @author agustinrodriguez
 */

// Inicialización de Stripe (Clave pública de Test)
// Nota: En producción, esta clave debe venir de una variable de entorno o configuración segura
const stripe = Stripe('pk_test_51SQcrdE9UFOohwbhnLd3oeK3hqG7D0YBzz5SGtkPMeOJModDhmr20d4fB4LkgovuMIjGS0hhb5M78HSM3vgiHUSF00EZRqlm29');

// Configuración de estilos para el Iframe de Stripe
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

let cardElement = null;

/**
 * Inicializa el formulario de pago al cargar la página
 */
function inicializarPasarelaPago() {
    const elements = stripe.elements();
    
    // Crear y montar el elemento de tarjeta
    cardElement = elements.create('card', {
        style: style,
        hidePostalCode: true // Simplifica el formulario si no necesitas validar zip
    });
    
    cardElement.mount('#card-element');

    // Listener para validación en tiempo real
    cardElement.on('change', function(event) {
        const displayError = document.getElementById('card-errors');
        if (event.error) {
            displayError.textContent = event.error.message;
        } else {
            displayError.textContent = '';
        }
    });

    // Listener para el envío del formulario
    const form = document.getElementById('payment-form');
    if (form) {
        form.addEventListener('submit', procesarPago);
    }
}

/**
 * Maneja el evento submit del formulario
 */
async function procesarPago(event) {
    event.preventDefault();
    
    const form = event.target;
    const submitButton = document.getElementById('submit-button');
    const buttonText = document.getElementById('button-text');
    const buttonSpinner = document.getElementById('button-spinner');

    // Bloquear UI para evitar doble envío
    submitButton.disabled = true;
    buttonText.style.display = 'none';
    buttonSpinner.style.display = 'inline-block';
    
    try {
        // Crear el PaymentMethod (Tokenización segura)
        const {paymentMethod, error} = await stripe.createPaymentMethod({
            type: 'card',
            card: cardElement,
        });
        
        if (error) {
            // Error en la validación de Stripe (Tarjeta inválida, fondos insuficientes...)
            const errorElement = document.getElementById('card-errors');
            errorElement.textContent = error.message;
            
            // Restaurar UI
            submitButton.disabled = false;
            buttonText.style.display = 'inline';
            buttonSpinner.style.display = 'none';
        } else {
            // Éxito: Inyectar el ID en el form y enviar al backend
            document.getElementById('payment-method-id').value = paymentMethod.id;
            form.submit();
        }
    } catch (err) {
        console.error('Error crítico al procesar el pago:', err);
        alert('Ocurrió un error inesperado. Por favor, recarga la página e inténtalo de nuevo.');
        
        submitButton.disabled = false;
        buttonText.style.display = 'inline';
        buttonSpinner.style.display = 'none';
    }
}

// Ejecutar inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', inicializarPasarelaPago);