package com.transporte.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.Pedido;
import com.transporte.service.PedidoService;

import jakarta.validation.Valid;

/** Endpoints REST de pedidos. Solo reciben la petición y llaman al Service. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // GET /api/pedidos?pendientes=true devuelve solo los pedidos que todavía no tienen envío
    @GetMapping
    public List<Pedido> listar(@RequestParam(defaultValue = "false") boolean pendientes) {
        return service.listar(pendientes);
    }

    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody Pedido datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @Valid @RequestBody Pedido datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
