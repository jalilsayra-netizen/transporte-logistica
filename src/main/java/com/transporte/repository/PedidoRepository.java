package com.transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.transporte.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByIdPedidoDesc();

    // Pedidos que todavía no tienen envío ("pendientes")
    @Query("select p from Pedido p where not exists (select e.idEnvio from Envio e where e.pedido = p) "
            + "order by p.idPedido desc")
    List<Pedido> buscarPendientes();

    @Query("select count(p) from Pedido p where not exists (select e.idEnvio from Envio e where e.pedido = p)")
    long contarPendientes();
}
