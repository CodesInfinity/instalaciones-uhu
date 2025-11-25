package app.modelos;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * ENTIDAD: USUARIO
 * * <p>Representa a un actor dentro del sistema (Administrador, Estudiante o Profesor).
 * Esta clase mapea la tabla <code>usuarios</code> de la base de datos.</p>
 * * <p><strong>Gestión de Roles:</strong></p>
 * <ul>
 * <li><strong>0:</strong> Administrador (Acceso total al panel de gestión).</li>
 * <li><strong>1:</strong> Estudiante (Rol por defecto, puede reservar pagando).</li>
 * <li><strong>2:</strong> Profesor (Puede reservar gratuitamente ciertos espacios).</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.0
 */
@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * Identificador único del usuario (Clave Primaria).
     * Generado automáticamente por la base de datos (Auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Documento Nacional de Identidad.
     * Se utiliza como identificación legal y debe ser único en el sistema.
     */
    @Column(unique = true, nullable = false)
    private String dni;
    
    /**
     * Nombre completo y apellidos del usuario.
     */
    private String nombre;
    
    /**
     * Correo electrónico.
     * Actúa como nombre de usuario para el inicio de sesión (Login).
     */
    @Column(unique = true, nullable = false)
    private String email;
    
    /**
     * Contraseña de acceso.
     * <p><strong>Seguridad:</strong> Se almacena encriptada (Hash MD5 en esta versión).</p>
     */
    private String password;
    
    /**
     * Nivel de privilegios del usuario.
     * 0 = Admin, 1 = Estudiante, 2 = Profesor.
     */
    private int rol;
    
    /**
     * Estado de la solicitud de ascenso a rol de Profesor.
     * <p>Valores posibles:</p>
     * <ul>
     * <li><code>null</code>: No ha solicitado nada.</li>
     * <li><code>"PENDIENTE"</code>: Solicitud enviada, esperando revisión del admin.</li>
     * <li><code>"APROBADA"</code>: El usuario ya es profesor (rol actualizado a 2).</li>
     * <li><code>"RECHAZADA"</code>: La solicitud fue denegada.</li>
     * </ul>
     */
    @Column(name = "solicitud_profesor", nullable = true)
    private String solicitudProfesor;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    
    /**
     * Constructor vacío requerido por la especificación de JPA.
     * No debe usarse para crear objetos en la lógica de negocio.
     */
    public Usuario() {
    }

    /**
     * Constructor principal para crear nuevos usuarios.
     * * @param dni Documento de identidad.
     * @param nombre Nombre completo.
     * @param email Correo electrónico (Login).
     * @param password Contraseña (ya hasheada).
     * @param rol Rol numérico inicial.
     */
    public Usuario(String dni, String nombre, String email, String password, int rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.solicitudProfesor = null;
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
    
    // ==========================================
    // MÉTODOS DE UTILIDAD Y LÓGICA DE DOMINIO
    // ==========================================
    
    /**
     * Verifica si el usuario tiene una solicitud de ascenso a profesor
     * pendiente de revisión.
     * * @return true si el estado es "PENDIENTE".
     */
    public boolean tieneSolicitudPendiente() {
        return "PENDIENTE".equals(solicitudProfesor);
    }
    
    // ==========================================
    // MÉTODOS OVERRIDE (JPA IDENTITY)
    // ==========================================
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    /**
     * Compara dos objetos Usuario basándose en su ID (Clave Primaria).
     * Esencial para el correcto funcionamiento en Colecciones y contextos de persistencia.
     */
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

    @Override
    public String toString() {
        return "app.modelo.Usuario[ id=" + id + ", email=" + email + " ]";
    }
}