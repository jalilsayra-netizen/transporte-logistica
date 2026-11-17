package com.transporte.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.transporte.entity.Usuario;
import com.transporte.repository.UsuarioRepository;

/** Le explica a Spring Security cómo buscar un usuario en NUESTRA tabla "usuarios". */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())                  // hash BCrypt guardado en la base
                .roles(usuario.getRol())                          // ADMIN pasa a ser ROLE_ADMIN, etc.
                .disabled(usuario.getEstado().equals("INACTIVO")) // un usuario inactivo no puede entrar
                .build();
    }
}
