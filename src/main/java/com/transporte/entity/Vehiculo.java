package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Vehículo de la flota. La capacidad se mide en kilogramos. */
@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehiculo;

    @NotBlank(message = "La patente es obligatoria")
    @Pattern(regexp = "^[A-Za-z]{2}\\d{3}[A-Za-z]{2}$|^[A-Za-z]{3}\\d{3}$", message = "La patente debe tener formato AB123CD o ABC123")
    @Column(unique = true, length = 10)
    private String patente;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca admite hasta 50 caracteres")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo admite hasta 50 caracteres")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1980, message = "El año debe ser 1980 o posterior")
    @Max(value = 2100, message = "El año no es válido")
    private Integer anio;

    @NotNull(message = "Debe seleccionar un tipo de vehículo")
    @ManyToOne
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad (kg) debe ser mayor a 0")
    private Double capacidad;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "DISPONIBLE|EN_TRANSITO|EN_MANTENIMIENTO|INACTIVO", message = "El estado debe ser uno de: DISPONIBLE, EN_TRANSITO, EN_MANTENIMIENTO, INACTIVO")
    private String estado = "DISPONIBLE";

    @Size(max = 255, message = "Las observaciones admiten hasta 255 caracteres")
    private String observaciones;

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public TipoVehiculo getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public Double getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Double capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
