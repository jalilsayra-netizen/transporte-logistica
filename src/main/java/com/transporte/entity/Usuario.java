package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Usuario del sistema. El password se guarda con BCrypt y nunca se envía al navegador (WRITE_ONLY). */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre admite hasta 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido admite hasta 60 caracteres")
    private String apellido;

    @NotBlank(message = "El usuario es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._-]{4,40}$", message = "El usuario debe tener entre 4 y 40 caracteres (letras, números, punto, guion)")
    @Column(unique = true, length = 40)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "ADMIN|OPERADOR|CONDUCTOR", message = "El rol debe ser ADMIN, OPERADOR o CONDUCTOR")
    private String rol;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|INACTIVO", message = "El estado debe ser uno de: ACTIVO, INACTIVO")
    private String estado = "ACTIVO";

    @OneToOne
    @JoinColumn(name = "id_conductor", unique = true)
    private Conductor conductor;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }
}
