<%-- 
    Document   : formInstalacion
    Created on : 6 nov 2025, 20:17:47
    Author     : agustinrodriguez
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="instalaciones-container">
    <div class="form-container">
        <div class="form-instalacion">
            <div class="form-header">
                <h2 class="form-title">
                    <c:choose>
                        <c:when test="${not empty instalacion}">
                            <i class="fas fa-edit"></i> Editar Instalación
                        </c:when>
                        <c:otherwise>
                            <i class="fas fa-plus-circle"></i> Nueva Instalación
                        </c:otherwise>
                    </c:choose>
                </h2>
            </div>

            <div class="form-body">
                <%-- IMPORTANTE: Añadido enctype="multipart/form-data" --%>
                <form action="${pageContext.request.contextPath}/instalaciones/guardar" 
                      method="post" id="formInstalacion" class="instalacion-form"
                      enctype="multipart/form-data">

                    <c:if test="${not empty instalacion}">
                        <input type="hidden" name="id" value="${instalacion.id}">
                        <%-- Campo para guardar la URL de la imagen actual --%>
                        <input type="hidden" name="imagenUrlActual" value="${instalacion.imagenUrl}">
                    </c:if>

                    <div class="form-group">
                        <label for="nombre" class="form-label">
                            <i class="fas fa-signature"></i> Nombre de la Instalación *
                        </label>
                        <input type="text" 
                               class="form-control" 
                               id="nombre" 
                               name="nombre" 
                               value="${not empty instalacion ? instalacion.nombre : ''}"
                               required
                               maxlength="100"
                               placeholder="Ej: Cancha Central de Fútbol">
                        <div class="form-help">
                            <i class="fas fa-info-circle"></i> Nombre descriptivo de la instalación.
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="tipo" class="form-label">
                                <i class="fas fa-tag"></i> Tipo de Instalación *
                            </label>
                            <select class="form-select" id="tipo" name="tipo" required>
                                <option value="">Seleccione un tipo...</option>
                                <option value="Fútbol" ${not empty instalacion && instalacion.tipo == 'Fútbol' ? 'selected' : ''}>
                                    Fútbol
                                </option>
                                <option value="Baloncesto" ${not empty instalacion && instalacion.tipo == 'Baloncesto' ? 'selected' : ''}>
                                    Baloncesto
                                </option>
                                <option value="Tenis" ${not empty instalacion && instalacion.tipo == 'Tenis' ? 'selected' : ''}>
                                    Tenis
                                </option>
                                <option value="Pádel" ${not empty instalacion && instalacion.tipo == 'Pádel' ? 'selected' : ''}>
                                    Pádel
                                </option>
                                <option value="Gimnasio" ${not empty instalacion && instalacion.tipo == 'Gimnasio' ? 'selected' : ''}>
                                    Gimnasio
                                </option>
                                <option value="Atletismo" ${not empty instalacion && instalacion.tipo == 'Atletismo' ? 'selected' : ''}>
                                    Atletismo
                                </option>
                                <option value="Otro" ${not empty instalacion && instalacion.tipo == 'Otro' ? 'selected' : ''}>
                                    Otro
                                </option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="ubicacion" class="form-label">
                                <i class="fas fa-map-marker-alt"></i> Ubicación *
                            </label>
                            <input type="text" 
                                   class="form-control" 
                                   id="ubicacion" 
                                   name="ubicacion" 
                                   value="${not empty instalacion ? instalacion.ubicacion : ''}"
                                   required
                                   maxlength="150"
                                   placeholder="Ej: Zona Deportiva Norte, Edificio Principal">
                            <div class="form-help">
                                <i class="fas fa-info-circle"></i> Ubicación específica dentro del complejo.
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="descripcion" class="form-label">
                            <i class="fas fa-align-left"></i> Descripción
                        </label>
                        <textarea class="form-control" 
                                  id="descripcion" 
                                  name="descripcion" 
                                  rows="4"
                                  maxlength="500"
                                  placeholder="Describe las características, equipamiento y capacidades de la instalación...">${not empty instalacion ? instalacion.descripcion : ''}</textarea>
                        <div class="form-help">
                            <i class="fas fa-info-circle"></i> 
                            Máximo 500 caracteres. 
                            <span id="contadorCaracteres" class="caracteres-contador">0/500</span>
                        </div>
                    </div>

                    <div class="form-group form-group-image">
                        <label class="form-label">
                            <i class="fas fa-image"></i> Imagen de la Instalación
                        </label>
                        <div class="image-preview" id="imagePreview" onclick="document.getElementById('imagenInput').click()">
                            <c:choose>
                                <c:when test="${not empty instalacion and not empty instalacion.imagenUrl}">
                                    <img src="${pageContext.request.contextPath}${instalacion.imagenUrl}" 
                                         alt="Vista previa" 
                                         id="previewImage">
                                </c:when>
                                <c:otherwise>
                                    <div class="image-preview-placeholder">
                                        <i class="fas fa-camera"></i>
                                        <span>Haz clic para seleccionar una imagen</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <input type="file" 
                               class="file-input" 
                               id="imagenInput" 
                               name="imagen"
                               accept="image/*"
                               onchange="previewImage(this)">
                        <%-- EL INPUT HIDDEN 'imagenUrl' HA SIDO ELIMINADO --%>
                        <div class="form-help">
                            <i class="fas fa-info-circle"></i> 
                            Formatos recomendados: JPG, PNG. Tamaño máximo: 2MB.
                        </div>
                    </div>

                    <div class="form-actions">
                        <a href="${pageContext.request.contextPath}/instalaciones/panel" 
                           class="btn btn-secondary">
                            <i class="fas fa-arrow-left"></i> Volver al Panel
                        </a>
                        <button type="submit" class="btn btn-primary">
                            <c:choose>
                                <c:when test="${not empty instalacion}">
                                    <i class="fas fa-save"></i> Actualizar Instalación
                                </c:when>
                                <c:otherwise>
                                    <i class="fas fa-plus-circle"></i> Crear Instalación
                                </c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    // Contador de caracteres para la descripción
    document.getElementById('descripcion').addEventListener('input', function () {
        const contador = document.getElementById('contadorCaracteres');
        contador.textContent = this.value.length + '/500';

        if (this.value.length > 500) {
            contador.classList.add('text-danger');
        } else {
            contador.classList.remove('text-danger');
        }
    });

    // Inicializar contador
    document.addEventListener('DOMContentLoaded', function () {
        const descripcion = document.getElementById('descripcion');
        const contador = document.getElementById('contadorCaracteres');
        contador.textContent = descripcion.value.length + '/500';
    });

    // Validación del formulario
    document.getElementById('formInstalacion').addEventListener('submit', function (e) {
        const nombre = document.getElementById('nombre').value.trim();
        const tipo = document.getElementById('tipo').value;
        const ubicacion = document.getElementById('ubicacion').value.trim();

        if (!nombre || !tipo || !ubicacion) {
            e.preventDefault();
            alert('Por favor, complete todos los campos obligatorios (*)');
            return false;
        }

        if (document.getElementById('descripcion').value.length > 500) {
            e.preventDefault();
            alert('La descripción no puede exceder los 500 caracteres');
            return false;
        }
    });

    // Preview de imagen (Script actualizado)
    function previewImage(input) {
        const preview = document.getElementById('imagePreview');
        
        if (input.files && input.files[0]) {
            const reader = new FileReader();

            reader.onload = function (e) {
                // Crear elemento img si no existe
                let img = preview.querySelector('img');
                if (!img) {
                    img = document.createElement('img');
                    img.id = 'previewImage';
                    preview.innerHTML = '';
                    preview.appendChild(img);
                }

                img.src = e.target.result;
                img.alt = 'Vista previa';

                // Ya no se actualiza ningún input hidden
            };

            reader.readAsDataURL(input.files[0]);
        }
    }
</script>