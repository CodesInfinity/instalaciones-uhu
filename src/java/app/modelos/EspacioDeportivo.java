package app.modelos;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * ENTIDAD: ESPACIO DEPORTIVO
 * * <p>Representa una instalación física o recurso deportivo gestionable en el sistema.
 * Es la entidad principal sobre la que se realizan las {@link Reserva}.</p>
 * * <p><strong>Atributos:</strong></p>
 * <ul>
 * <li><strong>Nombre:</strong> Identificador común (ej. "Pista Central").</li>
 * <li><strong>Tipo:</strong> Categoría (ej. "Pista de Tenis", "Pabellón").</li>
 * <li><strong>Imagen URL:</strong> Ruta relativa al recurso gráfico subido.</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.0
 */
@Entity
@Table(name = "espacios_deportivos")
public class EspacioDeportivo {
    
    /**
     * Identificador único de la instalación (Clave Primaria).
     * Generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nombre comercial o identificativo de la instalación.
     * Obligatorio, longitud máxima 100 caracteres.
     */
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    /**
     * Categoría deportiva de la instalación.
     * Utilizado para filtros y cálculos de precios (ej. 'Pabellón', 'Tenis').
     */
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;
    
    /**
     * Ubicación física dentro del campus o complejo.
     * Ej: "Zona Norte", "Edificio C".
     */
    @Column(name = "ubicacion", length = 150, nullable = false)
    private String ubicacion;
    
    /**
     * Descripción detallada de las características (suelo, iluminación, aforo).
     */
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    /**
     * Ruta relativa de almacenamiento de la imagen representativa.
     * Ej: "/img/instalaciones/temp/uuid.jpg".
     */
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    
    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    
    /**
     * Constructor vacío requerido por JPA.
     */
    public EspacioDeportivo() {}
    
    /**
     * Constructor para inicialización básica sin imagen.
     * @param nombre Nombre de la instalación.
     * @param tipo Categoría.
     * @param ubicacion Localización física.
     * @param descripcion Detalles adicionales.
     */
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor completo con recurso multimedia.
     * @param nombre Nombre de la instalación.
     * @param tipo Categoría.
     * @param ubicacion Localización física.
     * @param descripcion Detalles adicionales.
     * @param imagenUrl Ruta relativa de la imagen.
     */
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }
    
    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================
    
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
    
    public String getUbicacion() { 
        return ubicacion; 
    }
    public void setUbicacion(String ubicacion) { 
        this.ubicacion = ubicacion; 
    }
    
    public String getDescripcion() { 
        return descripcion; 
    }
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public String getImagenUrl() { 
        return imagenUrl; 
    }
    public void setImagenUrl(String imagenUrl) { 
        this.imagenUrl = imagenUrl; 
    }
    
    // ==========================================
    // MÉTODOS OVERRIDE (JPA IDENTITY)
    // ==========================================
    
    /**
     * Compara la igualdad basándose únicamente en el ID de la base de datos.
     * Esto asegura que dos objetos Java diferentes que refieren a la misma fila
     * de base de datos se consideren iguales.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EspacioDeportivo that = (EspacioDeportivo) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "EspacioDeportivo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}