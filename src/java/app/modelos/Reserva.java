/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app.modelos;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * ENTIDAD: RESERVA
 * * <p>Representa una transacción de reserva confirmada en el sistema.
 * Vincula a un {@link Usuario} con un {@link EspacioDeportivo} en un intervalo de tiempo específico.</p>
 * * <p><strong>Reglas de Dominio:</strong></p>
 * <ul>
 * <li>La duración estándar es de 90 minutos (gestionado por controlador).</li>
 * <li>No pueden existir dos reservas solapadas en el mismo espacio.</li>
 * <li>Se almacena la fecha de creación para auditoría.</li>
 * </ul>
 * * @author agustinrodriguez
 * @version 2.0
 */
@Entity
@Table(name = "reservas")
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificador único de la reserva (Clave Primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El usuario que realizó la reserva.
     * Relación: Muchas reservas pertenecen a un usuario.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * La instalación deportiva reservada.
     * Relación: Muchas reservas ocurren en un espacio.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private EspacioDeportivo espacio;

    /**
     * Fecha y hora exacta del inicio de la actividad.
     * Mapeado como TIMESTAMP en la base de datos.
     */
    @Column(name = "inicio", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime inicio;

    /**
     * Fecha y hora exacta del fin de la actividad.
     */
    @Column(name = "fin", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fin;

    /**
     * Marca de tiempo de cuando se creó el registro (Auditoría).
     * Se inicializa automáticamente al momento de instanciar el objeto.
     */
    @Column(name = "creado_en", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creadoEn = LocalDateTime.now();

    // ==========================================
    // CONSTRUCTORES
    // ==========================================

    /**
     * Constructor vacío requerido por JPA.
     */
    public Reserva() {
    }

    /**
     * Constructor principal para crear nuevas reservas.
     * La fecha de creación se establece automáticamente a <code>now()</code>.
     * * @param usuario El usuario titular.
     * @param espacio La instalación reservada.
     * @param inicio Hora de comienzo.
     * @param fin Hora de finalización.
     */
    public Reserva(Usuario usuario, EspacioDeportivo espacio, LocalDateTime inicio, LocalDateTime fin) {
        this.usuario = usuario;
        this.espacio = espacio;
        this.inicio = inicio;
        this.fin = fin;
        this.creadoEn = LocalDateTime.now();
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EspacioDeportivo getEspacio() {
        return espacio;
    }

    public void setEspacio(EspacioDeportivo espacio) {
        this.espacio = espacio;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    // ==========================================
    // MÉTODOS DE ADAPTACIÓN (VIEW HELPERS)
    // ==========================================
    
    /**
     * Convierte {@link LocalDateTime} a {@link java.util.Date}.
     * <p><strong>Propósito:</strong> Compatibilidad con la etiqueta JSTL <code>&lt;fmt:formatDate&gt;</code>
     * en las vistas JSP, ya que las versiones antiguas de JSTL no soportan la API moderna de Java Time.</p>
     * * @return Objeto Date representando el inicio.
     */
    public Date getInicioDate() {
        if (inicio == null) return null;
        return Date.from(inicio.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convierte {@link LocalDateTime} a {@link java.util.Date}.
     * <p><strong>Propósito:</strong> Compatibilidad con la etiqueta JSTL <code>&lt;fmt:formatDate&gt;</code>.</p>
     * * @return Objeto Date representando el fin.
     */
    public Date getFinDate() {
        if (fin == null) return null;
        return Date.from(fin.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convierte {@link LocalDateTime} a {@link java.util.Date}.
     * <p><strong>Propósito:</strong> Compatibilidad con la etiqueta JSTL <code>&lt;fmt:formatDate&gt;</code>.</p>
     * * @return Objeto Date representando la fecha de creación.
     */
    public Date getCreadoEnDate() {
        if (creadoEn == null) return null;
        return Date.from(creadoEn.atZone(ZoneId.systemDefault()).toInstant());
    }
}