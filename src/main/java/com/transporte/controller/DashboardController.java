package com.transporte.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporte.repository.ClienteRepository;
import com.transporte.repository.ConductorRepository;
import com.transporte.repository.EnvioRepository;
import com.transporte.repository.PedidoRepository;
import com.transporte.repository.UsuarioRepository;
import com.transporte.repository.VehiculoRepository;

/** Números para la pantalla Dashboard. El conductor solo ve los números de sus propios envíos. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final EnvioRepository envioRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(EnvioRepository envioRepository, ClienteRepository clienteRepository,
                               VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository,
                               PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public Map<String, Object> resumen(Authentication auth) {
        Map<String, Object> datos = new LinkedHashMap<>();
        List<String> estados = List.of("PREPARANDO", "EN_TRANSITO", "ENTREGADO", "CANCELADO");
        boolean esConductor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONDUCTOR"));

        if (esConductor) {
            Long idConductor = usuarioRepository.findByUsername(auth.getName())
                    .map(u -> u.getConductor() == null ? -1L : u.getConductor().getIdConductor()).orElse(-1L);
            for (String estado : estados) {
                datos.put(estado, envioRepository.countByConductorIdConductorAndEstadoNombre(idConductor, estado));
            }
            return datos;
        }
        for (String estado : estados) {
            datos.put(estado, envioRepository.countByEstadoNombre(estado));
        }
        datos.put("clientesActivos", clienteRepository.countByEstado("ACTIVO"));
        datos.put("vehiculosDisponibles", vehiculoRepository.countByEstado("DISPONIBLE"));
        datos.put("conductoresActivos", conductorRepository.countByEstado("ACTIVO"));
        datos.put("pedidosPendientes", pedidoRepository.contarPendientes());
        return datos;
    }
}
