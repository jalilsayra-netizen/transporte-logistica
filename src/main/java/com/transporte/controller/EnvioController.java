package com.transporte.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.transporte.entity.Entrega;
import com.transporte.entity.Envio;
import com.transporte.entity.HistorialEstado;
import com.transporte.service.EnvioService;

import jakarta.validation.Valid;

/** Endpoints REST de envíos. */
@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService service;

    public EnvioController(EnvioService service) {
        this.service = service;
    }

    // ---------- CRUD ----------

    @GetMapping
    public List<Envio> listar(@RequestParam(required = false) String estado, Authentication auth) {
        return service.listar(estado, auth);
    }

    @GetMapping("/{id}")
    public Envio buscarPorId(@PathVariable Long id, Authentication auth) {
        return service.buscarPorId(id, auth);
    }

    @PostMapping
    public ResponseEntity<Envio> crear(@Valid @RequestBody Envio datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos, auth));
    }

    @PutMapping("/{id}")
    public Envio actualizar(@PathVariable Long id, @Valid @RequestBody Envio datos, Authentication auth) {
        return service.actualizar(id, datos, auth);
    }

    // Un envío no se borra: se cancela y conserva su historial
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication auth) {
        service.cancelar(id, auth);
        return ResponseEntity.noContent().build();
    }

    // ---------- Endpoints especiales ----------

    // GET /api/envios/5/historial
    @GetMapping("/{id}/historial")
    public List<HistorialEstado> historial(@PathVariable Long id, Authentication auth) {
        return service.historial(id, auth);
    }

    // PUT /api/envios/5/estado   con el cuerpo {"estado": "EN_TRANSITO", "observaciones": "..."}
    @PutMapping("/{id}/estado")
    public Envio cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> cuerpo, Authentication auth) {
        return service.cambiarEstado(id, cuerpo.get("estado"), cuerpo.get("observaciones"), auth);
    }

    // PUT /api/envios/5/vehiculo/2
    @PutMapping("/{id}/vehiculo/{idVehiculo}")
    public Envio asignarVehiculo(@PathVariable Long id, @PathVariable Long idVehiculo) {
        return service.asignarVehiculo(id, idVehiculo);
    }

    // PUT /api/envios/5/conductor/1
    @PutMapping("/{id}/conductor/{idConductor}")
    public Envio asignarConductor(@PathVariable Long id, @PathVariable Long idConductor) {
        return service.asignarConductor(id, idConductor);
    }

    // POST /api/envios/5/entrega   con los datos de la entrega
    @PostMapping("/{id}/entrega")
    public ResponseEntity<Entrega> registrarEntrega(@PathVariable Long id, @RequestBody Entrega datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarEntrega(id, datos, auth));
    }
}
