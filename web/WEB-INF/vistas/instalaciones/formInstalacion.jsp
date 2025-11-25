<%-- 
    VISTA: FORMULARIO DE GESTIÓN DE INSTALACIONES
    Created on : 26 oct 2025
    Author     : agustinrodriguez
    Description: Formulario dual para crear o editar una instalación deportiva.
                 Soporta subida de imágenes (multipart/form-data) y previsualización.
                 Incluye validación de campos en el cliente.
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
                <form action="${pageContext.request.contextPath}/instalaciones/guardar" 
                      method="post" id="formInstalacion" class="instalacion-form"
                      enctype="multipart/form-data">

                    <c:if test="${not empty instalacion}">
                        <%-- ID necesario para que JPA haga merge() en lugar de persist() --%>
                        <input type="hidden" name="id" value="${instalacion.id}">
                        <%-- Preservar URL de imagen actual si el usuario no sube una nueva --%>
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
                                <option value="Fútbol" ${not empty instalacion && instalacion.tipo == 'Fútbol' ? 'selected' : ''}>Fútbol</option>
                                <option value="Baloncesto" ${not empty instalacion && instalacion.tipo == 'Baloncesto' ? 'selected' : ''}>Baloncesto</option>
                                <option value="Tenis" ${not empty instalacion && instalacion.tipo == 'Tenis' ? 'selected' : ''}>Tenis</option>
                                <option value="Pádel" ${not empty instalacion && instalacion.tipo == 'Pádel' ? 'selected' : ''}>Pádel</option>
                                <option value="Gimnasio" ${not empty instalacion && instalacion.tipo == 'Gimnasio' ? 'selected' : ''}>Gimnasio</option>
                                <option value="Atletismo" ${not empty instalacion && instalacion.tipo == 'Atletismo' ? 'selected' : ''}>Atletismo</option>
                                <option value="Otro" ${not empty instalacion && instalacion.tipo == 'Otro' ? 'selected' : ''}>Otro</option>
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
                            <span><i class="fas fa-info-circle"></i> Máximo 500 caracteres.</span>
                            <span id="contadorCaracteres" class="caracteres-contador">0/500</span>
                        </div>
                    </div>

                    <div class="form-group form-group-image">
                        <label class="form-label">
                            <i class="fas fa-image"></i> Imagen de la Instalación
                        </label>
                        
                        <div class="image-preview" id="imagePreview" onclick="document.getElementById('imagenInput').click()">
                            <c:choose>
                                <%-- Mostrar imagen actual si existe --%>
                                <c:when test="${not empty instalacion and not empty instalacion.imagenUrl}">
                                    <img src="${pageContext.request.contextPath}${instalacion.imagenUrl}" 
                                         alt="Vista previa" 
                                         id="previewImage">
                                </c:when>
                                <%-- Mostrar placeholder si no hay imagen --%>
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

<script src="${pageContext.request.contextPath}/scripts/form-instalacion.js"></script>