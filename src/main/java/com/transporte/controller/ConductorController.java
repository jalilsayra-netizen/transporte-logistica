package com.transporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.Conductor;
import com.transporte.service.ConductorService;

import jakarta.validation.Valid;

/** Endpoints REST de conductores. Solo reciben la petición y llaman al Service. */
@RestController
@RequestMapping("/api/conductores")
public class ConductorController {

    private final ConductorService service;

    public ConductorController(ConductorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Conductor> listar(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) String estado) {
        return service.listar(q, estado);
    }

    @GetMapping("/{id}")
    public Conductor buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Conductor> crear(@Valid @RequestBody Conductor datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public Conductor actualizar(@PathVariable Long id, @Valid @RequestBody Conductor datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
