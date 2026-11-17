package com.transporte.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdUsuarioNot(String username, Long idUsuario);

    boolean existsByConductorIdConductor(Long idConductor);

    boolean existsByConductorIdConductorAndIdUsuarioNot(Long idConductor, Long idUsuario);
}
