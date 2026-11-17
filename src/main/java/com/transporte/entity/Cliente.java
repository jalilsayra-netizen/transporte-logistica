package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Cliente que contrata el transporte. */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 100, message = "La razón social admite hasta 100 caracteres")
    private String razonSocial;

    @NotBlank(message = "El CUIT/DNI es obligatorio")
    @Pattern(regexp = "^\\d{2}-?\\d{8}-?\\d$|^\\d{7,8}$", message = "El CUIT (11 dígitos) o DNI (7 u 8 dígitos) no tiene un formato válido")
    @Column(unique = true, length = 20)
    private String cuit;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s()]{6,20}$", message = "El teléfono solo admite números, espacios, +, - y paréntesis (6 a 20 caracteres)")
    private String telefono;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "El domicilio es obligatorio")
    @Size(max = 150, message = "El domicilio admite hasta 150 caracteres")
    private String domicilio;

    @NotBlank(message = "La localidad es obligatoria")
    @Size(max = 80, message = "La localidad admite hasta 80 caracteres")
    private String localidad;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "El estado debe ser uno de: ACTIVO, INACTIVO")
    private String estado = "ACTIVO";

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
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

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
