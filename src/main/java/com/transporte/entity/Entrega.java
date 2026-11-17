package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Entrega registrada de un envío. No se modifica una vez guardada. */
@Entity
@Table(name = "entregas")
@Immutable
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntrega;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_envio", nullable = false, unique = true)
    private Envio envio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 100)
    private String personaRecibe;

    @Column(length = 8)
    private String dniRecibe;

    @Column(nullable = false, length = 255)
    private String observaciones;

    @Column(nullable = false, length = 30)
    private String estadoEntrega;

    @Column(length = 255)
    private String comprobante;

    public Long getIdEntrega() {
        return idEntrega;
    }

    public void setIdEntrega(Long idEntrega) {
        this.idEntrega = idEntrega;
    }

    public Envio getEnvio() {
        return envio;
    }

    public void setEnvio(Envio envio) {
        this.envio = envio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getPersonaRecibe() {
        return personaRecibe;
    }

    public void setPersonaRecibe(String personaRecibe) {
        this.personaRecibe = personaRecibe;
    }

    public String getDniRecibe() {
        return dniRecibe;
    }

    public void setDniRecibe(String dniRecibe) {
        this.dniRecibe = dniRecibe;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstadoEntrega() {
        return estadoEntrega;
    }

    public void setEstadoEntrega(String estadoEntrega) {
        this.estadoEntrega = estadoEntrega;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }
}
