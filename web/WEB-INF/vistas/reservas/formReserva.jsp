<%-- 
    Document   : formReserva.jsp
    Description: Formulario inteligente. Detecta automáticamente si es:
                 1. Edición (carga datos de BD).
                 2. Nueva desde Calendario (carga datos de URL).
                 3. Nueva en blanco.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="reserva-form-container">
    <div class="reserva-form-card">
        
        <div class="reserva-form-header">
            <h2>
                <c:choose>
                    <c:when test="${not empty reserva}"><i class="fas fa-edit"></i> Editar Reserva</c:when>
                    <c:otherwise><i class="fas fa-plus-circle"></i> Nueva Reserva</c:otherwise>
                </c:choose>
            </h2>
            <p>Completa los datos para realizar tu reserva</p>
        </div>

        <form action="${pageContext.request.contextPath}/reservas/${not empty reserva ? 'guardar' : 'crear'}" 
              method="post" id="reservaForm">

            <c:if test="${not empty reserva}">
                <input type="hidden" name="id" value="${reserva.id}">
            </c:if>

            <div class="reserva-form-steps">
                
                <div class="reserva-form-step">
                    <div class="reserva-form-step-header">
                        <div class="reserva-step-number">1</div>
                        <h3 class="reserva-step-title">Selecciona la Instalación</h3>
                    </div>
                    <div class="reserva-step-content">
                        <div class="form-group">
                            <label for="espacioId">Instalación Deportiva *</label>
                            <div class="input-wrapper">
                                <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                                </svg>
                                <select id="espacioId" name="espacioId" required>
                                    <option value="">Selecciona una instalación</option>
                                    <c:forEach var="espacio" items="${espacios}">
                                        <c:set var="isSelected" value="false" />
                                        
                                        <%-- Caso 1: Edición de reserva existente --%>
                                        <c:if test="${not empty reserva && reserva.espacio.id == espacio.id}">
                                            <c:set var="isSelected" value="true" />
                                        </c:if>
                                        
                                        <%-- Caso 2: Vengo del calendario (Controller attribute) --%>
                                        <c:if test="${espacioIdPreseleccionado == espacio.id}">
                                            <c:set var="isSelected" value="true" />
                                        </c:if>
                                        
                                        <%-- Caso 3: Vengo por URL directa (Param) --%>
                                        <c:if test="${param.espacioId == espacio.id}">
                                            <c:set var="isSelected" value="true" />
                                        </c:if>

                                        <option value="${espacio.id}" 
                                                data-nombre="${espacio.nombre}"
                                                data-tipo="${espacio.tipo}"
                                                ${isSelected ? 'selected' : ''}>
                                            ${espacio.nombre} - ${espacio.tipo}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="reserva-form-step">
                    <div class="reserva-form-step-header">
                        <div class="reserva-step-number">2</div>
                        <h3 class="reserva-step-title">Fecha y Horario</h3>
                    </div>
                    <div class="reserva-step-content">
                        <div class="form-row">
                            
                            <div class="form-group">
                                <label for="fecha">Fecha *</label>
                                <div class="input-wrapper">
                                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                                    </svg>
                                    <input type="date" id="fecha" name="fecha" required
                                           value="${not empty reserva ? reserva.inicio.toLocalDate() : (not empty fechaPreseleccionada ? fechaPreseleccionada : param.fecha)}"
                                           min="<%= java.time.LocalDate.now()%>">
                                </div>
                                <small class="form-hint" id="fechaHint">Selecciona un día entre semana</small>
                            </div>

                            <div class="form-group">
                                <label for="hora">Hora de Inicio *</label>
                                <div class="input-wrapper">
                                    <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                    </svg>
                                    <input type="time" id="hora" name="hora" required
                                           value="${not empty reserva ? reserva.inicio.toLocalTime() : (not empty horaPreseleccionada ? horaPreseleccionada : param.hora)}"
                                           min="08:30" max="19:00">
                                </div>
                                <small class="form-hint">Bloques de 90 min (ej. 08:30, 10:00)</small>
                                <small class="form-hint" id="horaError" style="color:#dc2626; display:none;"></small>
                            </div>
                        </div>
                    </div>
                </div>

                <c:if test="${empty reserva && sessionScope.usuario.rol != 0 && sessionScope.usuario.rol != 2}">
                    <div class="reserva-form-step">
                        <div class="reserva-form-step-header">
                            <div class="reserva-step-number">3</div>
                            <h3 class="reserva-step-title">Información de Pago</h3>
                        </div>
                        <div class="reserva-step-content">
                            <div class="tuo-checkbox-container">
                                <label class="tuo-checkbox">
                                    <input type="checkbox" id="tieneTuo" name="tieneTuo" value="true">
                                    <div class="tuo-checkbox-label">
                                        <strong>Tengo Tarjeta Universitaria (TUO)</strong>
                                        <span>Marca esta opción si eres estudiante con TUO para obtener descuento</span>
                                    </div>
                                </label>
                            </div>

                            <div class="reserva-precio-info" id="precioInfo" style="display: none;">
                                <div class="reserva-precio-header">
                                    <div class="reserva-precio-icon">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                        </svg>
                                    </div>
                                    <h4 class="reserva-precio-title">Total a Pagar</h4>
                                </div>
                                <div class="reserva-precio-amount" id="precioAmount">0.00 €</div>
                                <p class="reserva-precio-descripcion" id="precioDescripcion"></p>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/reservas/" class="btn-secondary">Cancelar</a>
                <button type="submit" class="btn-primary" id="submitBtn">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <c:choose>
                        <c:when test="${not empty reserva}">Guardar Cambios</c:when>
                        <c:otherwise><span id="btnText">Continuar</span></c:otherwise>
                    </c:choose>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    window.ReservaConfig = {
        esProfesor: ${sessionScope.usuario.rol == 2},
        esAdmin: ${sessionScope.usuario.rol == 0},
        esEdicion: ${not empty reserva}
    };
</script>

<script src="${pageContext.request.contextPath}/scripts/form-reserva.js"></script>