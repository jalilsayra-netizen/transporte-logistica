package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.entity.HistorialEstado;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByEnvioIdEnvioOrderByFechaAscIdHistorialAsc(Long idEnvio);
}
