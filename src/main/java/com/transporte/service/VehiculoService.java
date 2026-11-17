package com.transporte.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.TipoVehiculo;
import com.transporte.entity.Vehiculo;
import com.transporte.exception.NegocioException;
import com.transporte.repository.TipoVehiculoRepository;
import com.transporte.repository.VehiculoRepository;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository repository;
    private final TipoVehiculoRepository tipoRepository;

    public VehiculoService(VehiculoRepository repository, TipoVehiculoRepository tipoRepository) {
        this.repository = repository;
        this.tipoRepository = tipoRepository;
    }

    // texto: busca por patente, marca o modelo. estado: filtra (por ejemplo DISPONIBLE). Ambos son opcionales.
    public List<Vehiculo> listar(String texto, String estado) {
        List<Vehiculo> lista = (texto == null || texto.isBlank()) ? repository.findAll() : repository.buscar(texto.trim());
        if (estado == null || estado.isBlank()) {
            return lista;
        }
        return lista.stream().filter(v -> v.getEstado().equals(estado)).toList();
    }

    public Vehiculo buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Vehículo"));
    }

    public Vehiculo crear(Vehiculo vehiculo) {
        vehiculo.setIdVehiculo(null);
        prepararDatos(vehiculo);
        if (repository.existsByPatente(vehiculo.getPatente())) {
            throw new NegocioException("Vehículo duplicado: la patente ya está registrada");
        }
        return repository.save(vehiculo);
    }

    public Vehiculo actualizar(Long id, Vehiculo datos) {
        Vehiculo vehiculo = buscarPorId(id);
        prepararDatos(datos);
        if (repository.existsByPatenteAndIdVehiculoNot(datos.getPatente(), id)) {
            throw new NegocioException("Vehículo duplicado: la patente ya está registrada");
        }
        vehiculo.setPatente(datos.getPatente());
        vehiculo.setMarca(datos.getMarca());
        vehiculo.setModelo(datos.getModelo());
        vehiculo.setAnio(datos.getAnio());
        vehiculo.setTipoVehiculo(datos.getTipoVehiculo());
        vehiculo.setCapacidad(datos.getCapacidad());
        vehiculo.setEstado(datos.getEstado());
        vehiculo.setObservaciones(datos.getObservaciones());
        return repository.save(vehiculo);
    }

    public void darDeBaja(Long id) {
        buscarPorId(id).setEstado("INACTIVO");
    }

    // La patente se guarda en mayúsculas y el tipo de vehículo tiene que existir
    private void prepararDatos(Vehiculo vehiculo) {
        vehiculo.setPatente(vehiculo.getPatente().trim().toUpperCase());
        if (vehiculo.getTipoVehiculo().getIdTipo() == null) {
            throw new NegocioException("Debe seleccionar un tipo de vehículo");
        }
        TipoVehiculo tipo = tipoRepository.findById(vehiculo.getTipoVehiculo().getIdTipo())
                .orElseThrow(() -> NegocioException.noEncontrado("Tipo de vehículo"));
        vehiculo.setTipoVehiculo(tipo);
    }
}
