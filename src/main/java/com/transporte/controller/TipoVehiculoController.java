package com.transporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.TipoVehiculo;
import com.transporte.service.TipoVehiculoService;

import jakarta.validation.Valid;

/** Endpoints REST de tipos de vehículo. Solo reciben la petición y llaman al Service. */
@RestController
@RequestMapping("/api/tipos-vehiculo")
public class TipoVehiculoController {

    private final TipoVehiculoService service;

    public TipoVehiculoController(TipoVehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoVehiculo> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TipoVehiculo buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TipoVehiculo> crear(@Valid @RequestBody TipoVehiculo datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public TipoVehiculo actualizar(@PathVariable Long id, @Valid @RequestBody TipoVehiculo datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
