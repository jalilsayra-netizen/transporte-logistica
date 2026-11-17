package com.transporte.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.Cliente;
import com.transporte.exception.NegocioException;
import com.transporte.repository.ClienteRepository;

/** Lógica de clientes. El Controller llama acá y acá se llama al Repository. */
@Service
@Transactional
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listar(String texto) {
        if (texto == null || texto.isBlank()) {
            return repository.findAll();
        }
        return repository.buscar(texto.trim());
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Cliente"));
    }

    public Cliente crear(Cliente cliente) {
        cliente.setIdCliente(null);
        if (repository.existsByCuit(cliente.getCuit().trim())) {
            throw new NegocioException("El cliente ya se encuentra registrado");
        }
        cliente.setCuit(cliente.getCuit().trim());
        return repository.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datos) {
        Cliente cliente = buscarPorId(id);
        if (repository.existsByCuitAndIdClienteNot(datos.getCuit().trim(), id)) {
            throw new NegocioException("El cliente ya se encuentra registrado");
        }
        cliente.setRazonSocial(datos.getRazonSocial());
        cliente.setCuit(datos.getCuit().trim());
        cliente.setTelefono(datos.getTelefono());
        cliente.setEmail(datos.getEmail());
        cliente.setDomicilio(datos.getDomicilio());
        cliente.setLocalidad(datos.getLocalidad());
        cliente.setEstado(datos.getEstado());
        return repository.save(cliente);
    }

    // Baja lógica: no se borra el registro, queda INACTIVO
    public void darDeBaja(Long id) {
        buscarPorId(id).setEstado("INACTIVO");
    }
}
