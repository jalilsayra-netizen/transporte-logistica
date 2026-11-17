package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Registro de cada cambio de estado. @Immutable = una vez guardado no se modifica (RN07). */
@Entity
@Table(name = "historial_estados")
@Immutable
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorial;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_envio", nullable = false)
    private Envio envio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoEnvio estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String observaciones;

    @Column(length = 40)
    private String usuario;

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
