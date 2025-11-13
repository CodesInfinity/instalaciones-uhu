package app.modelos;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * MODELO: ESPACIO DEPORTIVO
 * 
 * Representa una instalación deportiva en el sistema
 * Campos: nombre, tipo, ubicación, descripción, imagen
 * 
 * @author agustinrodriguez
 * @version 2.0 - Refactorizado y comentado
 */
@Entity
@Table(name = "espacios_deportivos")
public class EspacioDeportivo {
    
    // Identificador único autoincrementable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Nombre de la instalación (máximo 100 caracteres, obligatorio)
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    // Tipo de instalación: Fútbol, Baloncesto, Tenis, etc. (máximo 50 caracteres)
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;
    
    // Ubicación de la instalación (máximo 150 caracteres)
    @Column(name = "ubicacion", length = 150, nullable = false)
    private String ubicacion;
    
    // Descripción detallada (máximo 500 caracteres)
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    // URL de la imagen de la instalación
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    
    // ===== CONSTRUCTORES =====
    
    /**
     * Constructor vacío (requerido por JPA)
     */
    public EspacioDeportivo() {}
    
    /**
     * Constructor con parámetros básicos
     */
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor con todos los parámetros incluyendo imagen
     */
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }
    
    // ===== GETTERS Y SETTERS =====
    
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
    
    // ===== MÉTODOS EQUALS Y HASHCODE =====
    
    /**
     * Compara dos espacios deportivos por su ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EspacioDeportivo that = (EspacioDeportivo) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * Genera un hash code basado en el ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // ===== MÉTODO TOSTRING =====
    
    /**
     * Representación en string del objeto para debugging
     */
    @Override
    public String toString() {
        return "EspacioDeportivo{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}
