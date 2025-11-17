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
 * MODELO: RESERVA
 *
 * Representa una reserva realizada por un usuario sobre una instalación
 * (EspacioDeportivo) Restricciones importantes aplicadas en el controlador: -
 * Cada reserva debe tener una duración EXACTA de 1 hora y 30 minutos. - Las
 * colisiones entre reservas para la misma instalación deben evitarse.
 *
 * Campos: id, usuario, espacio, inicio, fin, creadoEn
 *
 * @author asistente
 */
@Entity
@Table(name = "reservas")
public class Reserva implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "espacio_id", nullable = false)
    private EspacioDeportivo espacio;

    @Column(name = "inicio", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime inicio;

    @Column(name = "fin", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime fin;

    @Column(name = "creado_en", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creadoEn = LocalDateTime.now();

// ===== Constructores =====
    public Reserva() {
    }

    public Reserva(Usuario usuario, EspacioDeportivo espacio, LocalDateTime inicio, LocalDateTime fin) {
        this.usuario = usuario;
        this.espacio = espacio;
        this.inicio = inicio;
        this.fin = fin;
        this.creadoEn = LocalDateTime.now();
    }

// ===== Getters / Setters =====
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

    /**
     * Convierte el campo inicio (LocalDateTime) a java.util.Date
     * para compatibilidad con JSTL fmt:formatDate
     */
    public Date getInicioDate() {
        if (inicio == null) return null;
        return Date.from(inicio.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convierte el campo fin (LocalDateTime) a java.util.Date
     * para compatibilidad con JSTL fmt:formatDate
     */
    public Date getFinDate() {
        if (fin == null) return null;
        return Date.from(fin.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convierte el campo creadoEn (LocalDateTime) a java.util.Date
     * para compatibilidad con JSTL fmt:formatDate
     */
    public Date getCreadoEnDate() {
        if (creadoEn == null) return null;
        return Date.from(creadoEn.atZone(ZoneId.systemDefault()).toInstant());
    }
}
