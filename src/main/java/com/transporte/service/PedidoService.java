package com.transporte.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.Cliente;
import com.transporte.entity.Pedido;
import com.transporte.exception.NegocioException;
import com.transporte.repository.ClienteRepository;
import com.transporte.repository.EnvioRepository;
import com.transporte.repository.PedidoRepository;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository repository;
    private final ClienteRepository clienteRepository;
    private final EnvioRepository envioRepository;

    public PedidoService(PedidoRepository repository, ClienteRepository clienteRepository,
                         EnvioRepository envioRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.envioRepository = envioRepository;
    }

    // soloPendientes = true devuelve únicamente los pedidos que todavía no tienen envío
    public List<Pedido> listar(boolean soloPendientes) {
        return soloPendientes ? repository.buscarPendientes() : repository.findAllByOrderByIdPedidoDesc();
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Pedido"));
    }

    public Pedido crear(Pedido pedido) {
        pedido.setIdPedido(null);
        pedido.setFecha(LocalDateTime.now());
        validar(pedido);
        return repository.save(pedido);
    }

    public Pedido actualizar(Long id, Pedido datos) {
        Pedido pedido = buscarPorId(id);
        if (envioRepository.existsByPedidoIdPedido(id)) {
            throw new NegocioException("El pedido ya tiene un envío y no puede modificarse");
        }
        validar(datos);
        pedido.setCliente(datos.getCliente());
        pedido.setOrigen(datos.getOrigen());
        pedido.setDestino(datos.getDestino());
        pedido.setTipoCarga(datos.getTipoCarga());
        pedido.setCantidad(datos.getCantidad());
        pedido.setPeso(datos.getPeso());
        pedido.setObservaciones(datos.getObservaciones());
        pedido.setFechaEstimadaEntrega(datos.getFechaEstimadaEntrega());
        return repository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        if (envioRepository.existsByPedidoIdPedido(id)) {
            throw new NegocioException("El pedido ya tiene un envío y no puede eliminarse");
        }
        repository.delete(pedido);
    }

    // El cliente tiene que existir y estar activo; la fecha de entrega no puede ser anterior a hoy
    private void validar(Pedido pedido) {
        if (pedido.getCliente().getIdCliente() == null) {
            throw new NegocioException("Datos incorrectos: debe seleccionar un cliente");
        }
        Cliente cliente = clienteRepository.findById(pedido.getCliente().getIdCliente()).orElse(null);
        if (cliente == null || !cliente.getEstado().equals("ACTIVO")) {
            throw new NegocioException("Cliente no válido");
        }
        pedido.setCliente(cliente);
        if (pedido.getFechaEstimadaEntrega().isBefore(LocalDate.now())) {
            throw new NegocioException("Datos incorrectos: la fecha estimada de entrega no puede ser anterior a hoy");
        }
    }
}
