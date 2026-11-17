package com.transporte.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.Conductor;
import com.transporte.exception.NegocioException;
import com.transporte.repository.ConductorRepository;

@Service
@Transactional
public class ConductorService {

    private final ConductorRepository repository;

    public ConductorService(ConductorRepository repository) {
        this.repository = repository;
    }

    public List<Conductor> listar(String texto, String estado) {
        List<Conductor> lista = (texto == null || texto.isBlank()) ? repository.findAll() : repository.buscar(texto.trim());
        if (estado == null || estado.isBlank()) {
            return lista;
        }
        return lista.stream().filter(c -> c.getEstado().equals(estado)).toList();
    }

    public Conductor buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Conductor"));
    }

    public Conductor crear(Conductor conductor) {
        conductor.setIdConductor(null);
        if (conductor.getVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new NegocioException("Datos incorrectos: la licencia está vencida");
        }
        if (repository.existsByDni(conductor.getDni()) || repository.existsByLicencia(conductor.getLicencia())) {
            throw new NegocioException("El conductor ya existe (DNI o licencia repetidos)");
        }
        return repository.save(conductor);
    }

    public Conductor actualizar(Long id, Conductor datos) {
        Conductor conductor = buscarPorId(id);
        if (repository.existsByDniAndIdConductorNot(datos.getDni(), id)
                || repository.existsByLicenciaAndIdConductorNot(datos.getLicencia(), id)) {
            throw new NegocioException("El conductor ya existe (DNI o licencia repetidos)");
        }
        conductor.setDni(datos.getDni());
        conductor.setApellido(datos.getApellido());
        conductor.setNombre(datos.getNombre());
        conductor.setLicencia(datos.getLicencia());
        conductor.setVencimientoLicencia(datos.getVencimientoLicencia());
        conductor.setTelefono(datos.getTelefono());
        conductor.setEmail(datos.getEmail());
        conductor.setEstado(datos.getEstado());
        return repository.save(conductor);
    }

    public void darDeBaja(Long id) {
        buscarPorId(id).setEstado("INACTIVO");
    }
}
