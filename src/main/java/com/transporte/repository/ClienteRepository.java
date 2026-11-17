package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transporte.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCuit(String cuit);

    boolean existsByCuitAndIdClienteNot(String cuit, Long idCliente);

    long countByEstado(String estado);

    // :q es un parámetro: el texto del usuario nunca se pega dentro de la consulta (evita SQL Injection)
    @Query("select c from Cliente c where lower(c.razonSocial) like lower(concat('%', :q, '%')) "
            + "or c.cuit like concat('%', :q, '%') or lower(c.localidad) like lower(concat('%', :q, '%')) "
            + "order by c.razonSocial")
    List<Cliente> buscar(@Param("q") String q);
}
