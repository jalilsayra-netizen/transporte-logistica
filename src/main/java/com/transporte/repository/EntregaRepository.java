package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.entity.Entrega;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findAllByOrderByIdEntregaDesc();

    List<Entrega> findByEnvioConductorIdConductorOrderByIdEntregaDesc(Long idConductor);
}
