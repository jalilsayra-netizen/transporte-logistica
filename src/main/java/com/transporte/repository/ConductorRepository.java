package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transporte.entity.Conductor;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    boolean existsByDni(String dni);

    boolean existsByDniAndIdConductorNot(String dni, Long idConductor);

    boolean existsByLicencia(String licencia);

    boolean existsByLicenciaAndIdConductorNot(String licencia, Long idConductor);

    long countByEstado(String estado);

    @Query("select c from Conductor c where lower(c.apellido) like lower(concat('%', :q, '%')) "
            + "or lower(c.nombre) like lower(concat('%', :q, '%')) or c.dni like concat('%', :q, '%') "
            + "or lower(c.licencia) like lower(concat('%', :q, '%')) order by c.apellido, c.nombre")
    List<Conductor> buscar(@Param("q") String q);
}
