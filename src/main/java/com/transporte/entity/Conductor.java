package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Conductor que realiza los viajes. */
@Entity
@Table(name = "conductores")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConductor;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe tener 7 u 8 dígitos")
    @Column(unique = true, length = 8)
    private String dni;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido admite hasta 60 caracteres")
    private String apellido;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre admite hasta 60 caracteres")
    private String nombre;

    @NotBlank(message = "El número de licencia es obligatorio")
    @Size(max = 30, message = "La licencia admite hasta 30 caracteres")
    @Column(unique = true, length = 30)
    private String licencia;

    @NotNull(message = "El vencimiento de la licencia es obligatorio")
    private LocalDate vencimientoLicencia;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s()]{6,20}$", message = "El teléfono solo admite números, espacios, +, - y paréntesis (6 a 20 caracteres)")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|INACTIVO|DE_LICENCIA", message = "El estado debe ser uno de: ACTIVO, INACTIVO, DE_LICENCIA")
    private String estado = "ACTIVO";

    public Long getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(Long idConductor) {
        this.idConductor = idConductor;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public LocalDate getVencimientoLicencia() {
        return vencimientoLicencia;
    }

    public void setVencimientoLicencia(LocalDate vencimientoLicencia) {
        this.vencimientoLicencia = vencimientoLicencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
