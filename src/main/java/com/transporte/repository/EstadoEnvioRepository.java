package com.transporte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.entity.EstadoEnvio;

public interface EstadoEnvioRepository extends JpaRepository<EstadoEnvio, Long> {

    Optional<EstadoEnvio> findByNombre(String nombre);
}
