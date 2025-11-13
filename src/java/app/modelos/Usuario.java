package app.modelos;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * MODELO: USUARIO
 * 
 * Representa un usuario del sistema
 * Campos: dni, nombre, email, contraseña, rol, solicitud profesor
 * Roles: 0=Admin, 1=Estudiante, 2=Profesor
 * 
 * @author agustinrodriguez
 * @version 2.0 - Refactorizado y comentado
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // Identificador único autoincrementable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // DNI del usuario (documento nacional de identidad)
    private String dni;
    
    // Nombre completo del usuario
    private String nombre;
    
    // Email único del usuario
    private String email;
    
    // Contraseña encriptada
    private String password;
    
    // Rol del usuario: 0=Administrador, 1=Estudiante, 2=Profesor
    private int rol;
    
    // Estado de solicitud de profesor: null, "PENDIENTE", "APROBADA", "RECHAZADA"
    @Column(name = "solicitud_profesor", nullable = true)
    private String solicitudProfesor;

    // ===== CONSTRUCTORES =====
    
    /**
     * Constructor vacío (requerido por JPA)
     */
    public Usuario() {
    }

    /**
     * Constructor con parámetros principales
     */
    public Usuario(String dni, String nombre, String email, String password, int rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.solicitudProfesor = null;
    }

    // ===== GETTERS Y SETTERS =====
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }
    
    public String getSolicitudProfesor() {
        return solicitudProfesor;
    }

    public void setSolicitudProfesor(String solicitudProfesor) {
        this.solicitudProfesor = solicitudProfesor;
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    /**
     * Método conveniente para verificar si el usuario tiene solicitud de profesor pendiente
     */
    public boolean tieneSolicitudPendiente() {
        return "PENDIENTE".equals(solicitudProfesor);
    }
    
    // ===== MÉTODOS EQUALS Y HASHCODE =====
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    // ===== MÉTODO TOSTRING =====
    
    @Override
    public String toString() {
        return "app.modelo.Usuario[ id=" + id + " ]";
    }
}
