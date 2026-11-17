package com.transporte.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transporte.entity.Envio;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    boolean existsByPedidoIdPedido(Long idPedido);

    List<Envio> findAllByOrderByIdEnvioDesc();

    List<Envio> findByEstadoNombreOrderByIdEnvioDesc(String estado);

    List<Envio> findByConductorIdConductorOrderByIdEnvioDesc(Long idConductor);

    List<Envio> findByConductorIdConductorAndEstadoNombreOrderByIdEnvioDesc(Long idConductor, String estado);

    long countByEstadoNombre(String estado);

    long countByConductorIdConductorAndEstadoNombre(Long idConductor, String estado);

    // RN03: cuenta los envíos activos del vehículo que se superponen con el horario [desde, hasta]
    @Query("select count(e) from Envio e where e.vehiculo.idVehiculo = :idVehiculo and e.idEnvio <> :idEnvio "
            + "and e.estado.nombre in ('PREPARANDO', 'EN_TRANSITO') "
            + "and e.fechaSalida < :hasta and e.fechaEstimadaLlegada > :desde")
    long contarSolapadosVehiculo(@Param("idVehiculo") Long idVehiculo, @Param("idEnvio") Long idEnvio,
                                 @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // RN03: lo mismo para el conductor
    @Query("select count(e) from Envio e where e.conductor.idConductor = :idConductor and e.idEnvio <> :idEnvio "
            + "and e.estado.nombre in ('PREPARANDO', 'EN_TRANSITO') "
            + "and e.fechaSalida < :hasta and e.fechaEstimadaLlegada > :desde")
    long contarSolapadosConductor(@Param("idConductor") Long idConductor, @Param("idEnvio") Long idEnvio,
                                  @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
