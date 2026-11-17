package com.transporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.Usuario;
import com.transporte.service.UsuarioService;

import jakarta.validation.Valid;

/** Endpoints REST de usuarios (solo ADMIN). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Usuario> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @Valid @RequestBody Usuario datos) {
        return service.actualizar(id, datos);
    }

    // Authentication = el usuario que está logueado (lo completa Spring Security)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id, Authentication auth) {
        service.darDeBaja(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
