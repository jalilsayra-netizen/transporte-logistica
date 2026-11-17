package com.transporte.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporte.entity.Entrega;
import com.transporte.service.EnvioService;

/** Consulta de entregas. Para registrar una se usa POST /api/envios/{id}/entrega. */
@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EnvioService service;

    public EntregaController(EnvioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Entrega> listar(Authentication auth) {
        return service.listarEntregas(auth);
    }

    @GetMapping("/{id}")
    public Entrega buscarPorId(@PathVariable Long id, Authentication auth) {
        return service.buscarEntrega(id, auth);
    }
}
