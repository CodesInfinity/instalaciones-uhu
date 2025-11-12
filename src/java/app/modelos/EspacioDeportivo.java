package app.modelos;

import jakarta.persistence.*;
import java.util.Objects;

/**
 *
 * @author agustinrodriguez
 */
@Entity
@Table(name = "espacios_deportivos")
public class EspacioDeportivo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;
    
    @Column(name = "ubicacion", length = 150, nullable = false)
    private String ubicacion;
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;
    
    // Constructores
    public EspacioDeportivo() {}
    
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
    }
    
    public EspacioDeportivo(String nombre, String tipo, String ubicacion, String descripcion, String imagen_url) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.imagenUrl = imagen_url;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    // equals y hashCode
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
    
    // toString
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