package com.transporte.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.TipoVehiculo;
import com.transporte.exception.NegocioException;
import com.transporte.repository.TipoVehiculoRepository;
import com.transporte.repository.VehiculoRepository;

@Service
@Transactional
public class TipoVehiculoService {

    private final TipoVehiculoRepository repository;
    private final VehiculoRepository vehiculoRepository;

    public TipoVehiculoService(TipoVehiculoRepository repository, VehiculoRepository vehiculoRepository) {
        this.repository = repository;
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<TipoVehiculo> listar() {
        return repository.findAll();
    }

    public TipoVehiculo buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Tipo de vehículo"));
    }

    public TipoVehiculo crear(TipoVehiculo tipo) {
        tipo.setIdTipo(null);
        tipo.setNombre(tipo.getNombre().trim());
        if (repository.existsByNombreIgnoreCase(tipo.getNombre())) {
            throw new NegocioException("El tipo de vehículo ya existe");
        }
        return repository.save(tipo);
    }

    public TipoVehiculo actualizar(Long id, TipoVehiculo datos) {
        TipoVehiculo tipo = buscarPorId(id);
        String nombre = datos.getNombre().trim();
        if (!tipo.getNombre().equalsIgnoreCase(nombre) && repository.existsByNombreIgnoreCase(nombre)) {
            throw new NegocioException("El tipo de vehículo ya existe");
        }
        tipo.setNombre(nombre);
        return repository.save(tipo);
    }

    public void eliminar(Long id) {
        TipoVehiculo tipo = buscarPorId(id);
        if (vehiculoRepository.existsByTipoVehiculoIdTipo(id)) {
            throw new NegocioException("No se puede eliminar: hay vehículos de este tipo");
        }
        repository.delete(tipo);
    }
}
