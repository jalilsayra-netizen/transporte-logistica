package com.transporte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.Immutable;

/** Pedido de transporte de un cliente. El peso se mide en kilogramos. */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @NotBlank(message = "La dirección de origen es obligatoria")
    @Size(max = 200, message = "El origen admite hasta 200 caracteres")
    private String origen;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 200, message = "El destino admite hasta 200 caracteres")
    private String destino;

    @NotBlank(message = "El tipo de carga es obligatorio")
    @Size(max = 100, message = "El tipo de carga admite hasta 100 caracteres")
    private String tipoCarga;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso (kg) debe ser mayor a 0")
    private Double peso;

    @Size(max = 255, message = "Las observaciones admiten hasta 255 caracteres")
    private String observaciones;

    @NotNull(message = "La fecha estimada de entrega es obligatoria")
    private LocalDate fechaEstimadaEntrega;

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getTipoCarga() {
        return tipoCarga;
    }

    public void setTipoCarga(String tipoCarga) {
        this.tipoCarga = tipoCarga;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    public void setFechaEstimadaEntrega(LocalDate fechaEstimadaEntrega) {
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }
}
