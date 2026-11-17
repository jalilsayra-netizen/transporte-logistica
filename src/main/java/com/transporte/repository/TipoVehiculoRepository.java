package com.transporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.entity.TipoVehiculo;

public interface TipoVehiculoRepository extends JpaRepository<TipoVehiculo, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
