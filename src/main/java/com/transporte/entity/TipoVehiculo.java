package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Tipo de vehículo (camión, camioneta, furgón...). */
@Entity
@Table(name = "tipos_vehiculo")
public class TipoVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipo;

    @NotBlank(message = "El nombre del tipo es obligatorio")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
    @Column(unique = true, length = 50)
    private String nombre;

    public Long getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
