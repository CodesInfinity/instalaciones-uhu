/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author agusm
 */
@Entity
@Table(name="usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dni;
    private String nombre;
    private String email;
    private String password;
    private int rol;
    
    //Estado de solicitud de profesor
    @Column(name = "solicitud_profesor", nullable = true)
    private String solicitudProfesor; // null=no solicitado, "PENDIENTE", "APROBADA", "RECHAZADA"

    public Usuario() {
    }

    public Usuario(String dni, String nombre, String email, String password, int rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.solicitudProfesor = null; // Por defecto no tiene solicitud
    }

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
    
    // NUEVOS GETTERS Y SETTERS
    public String getSolicitudProfesor() {
        return solicitudProfesor;
    }

    public void setSolicitudProfesor(String solicitudProfesor) {
        this.solicitudProfesor = solicitudProfesor;
    }
    
    // MÉTODO CONVENIENTE PARA VERIFICAR SI TIENE SOLICITUD PENDIENTE
    public boolean tieneSolicitudPendiente() {
        return "PENDIENTE".equals(solicitudProfesor);
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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
        return "app.modelo.Usuario[ id=" + id + " ]";
    }
    
}