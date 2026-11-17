package com.transporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.Vehiculo;
import com.transporte.service.VehiculoService;

import jakarta.validation.Valid;

/** Endpoints REST de vehículos. Solo reciben la petición y llaman al Service. */
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService service;

    public VehiculoController(VehiculoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Vehiculo> listar(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) String estado) {
        return service.listar(q, estado);
    }

    @GetMapping("/{id}")
    public Vehiculo buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody Vehiculo datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public Vehiculo actualizar(@PathVariable Long id, @Valid @RequestBody Vehiculo datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
