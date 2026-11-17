package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Envío: sale de un pedido, usa un vehículo y un conductor, y guarda su historial de estados. */
@Entity
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEnvio;

    @NotNull(message = "Debe seleccionar un pedido")
    @OneToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "id_conductor")
    private Conductor conductor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoEnvio estado;

    @NotNull(message = "La fecha de salida es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaSalida;

    @NotNull(message = "La fecha estimada de llegada es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaEstimadaLlegada;

    @NotBlank(message = "El itinerario es obligatorio")
    @Size(max = 255, message = "El itinerario admite hasta 255 caracteres")
    private String itinerario;

    @Size(max = 255, message = "Las observaciones admiten hasta 255 caracteres")
    private String observacionesRuta;

    public Long getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(Long idEnvio) {
        this.idEnvio = idEnvio;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaEstimadaLlegada() {
        return fechaEstimadaLlegada;
    }

    public void setFechaEstimadaLlegada(LocalDateTime fechaEstimadaLlegada) {
        this.fechaEstimadaLlegada = fechaEstimadaLlegada;
    }

    public String getItinerario() {
        return itinerario;
    }

    public void setItinerario(String itinerario) {
        this.itinerario = itinerario;
    }

    public String getObservacionesRuta() {
        return observacionesRuta;
    }

    public void setObservacionesRuta(String observacionesRuta) {
        this.observacionesRuta = observacionesRuta;
    }
}
