package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transporte.entity.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPatente(String patente);

    boolean existsByPatenteAndIdVehiculoNot(String patente, Long idVehiculo);

    boolean existsByTipoVehiculoIdTipo(Long idTipo);

    long countByEstado(String estado);

    @Query("select v from Vehiculo v where lower(v.patente) like lower(concat('%', :q, '%')) "
            + "or lower(v.marca) like lower(concat('%', :q, '%')) or lower(v.modelo) like lower(concat('%', :q, '%')) "
            + "order by v.patente")
    List<Vehiculo> buscar(@Param("q") String q);
}
