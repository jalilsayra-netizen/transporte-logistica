package com.transporte.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.Conductor;
import com.transporte.entity.Usuario;
import com.transporte.exception.NegocioException;
import com.transporte.repository.ConductorRepository;
import com.transporte.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ConductorRepository conductorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, ConductorRepository conductorRepository,
                          PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.conductorRepository = conductorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Usuario"));
    }

    public Usuario crear(Usuario datos) {
        if (datos.getPassword() == null || datos.getPassword().length() < 6) {
            throw new NegocioException("Datos incorrectos: la contraseña debe tener al menos 6 caracteres");
        }
        if (repository.existsByUsername(datos.getUsername().trim())) {
            throw new NegocioException("El usuario ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(datos.getUsername().trim());
        // Nunca se guarda la contraseña real: se guarda su "hash" (BCrypt)
        usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        copiarDatos(usuario, datos, null);
        return repository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario usuario = buscarPorId(id);
        if (repository.existsByUsernameAndIdUsuarioNot(datos.getUsername().trim(), id)) {
            throw new NegocioException("El usuario ya existe");
        }
        usuario.setUsername(datos.getUsername().trim());
        // Si la contraseña viene vacía se conserva la que ya tenía
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            if (datos.getPassword().length() < 6) {
                throw new NegocioException("Datos incorrectos: la contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        copiarDatos(usuario, datos, id);
        return repository.save(usuario);
    }

    public void darDeBaja(Long id, String usuarioLogueado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getUsername().equals(usuarioLogueado)) {
            throw new NegocioException("No puede dar de baja su propio usuario");
        }
        usuario.setEstado("INACTIVO");
    }

    // Copia los datos comunes. Si el rol es CONDUCTOR, el usuario tiene que estar asociado a un conductor.
    private void copiarDatos(Usuario usuario, Usuario datos, Long idUsuario) {
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setRol(datos.getRol());
        usuario.setEstado(datos.getEstado());

        if (!datos.getRol().equals("CONDUCTOR")) {
            usuario.setConductor(null);
            return;
        }
        if (datos.getConductor() == null || datos.getConductor().getIdConductor() == null) {
            throw new NegocioException("Datos incorrectos: un usuario CONDUCTOR debe estar asociado a un conductor");
        }
        Long idConductor = datos.getConductor().getIdConductor();
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> NegocioException.noEncontrado("Conductor"));
        boolean yaTieneUsuario = (idUsuario == null)
                ? repository.existsByConductorIdConductor(idConductor)
                : repository.existsByConductorIdConductorAndIdUsuarioNot(idConductor, idUsuario);
        if (yaTieneUsuario) {
            throw new NegocioException("Ese conductor ya tiene un usuario asociado");
        }
        usuario.setConductor(conductor);
    }
}
