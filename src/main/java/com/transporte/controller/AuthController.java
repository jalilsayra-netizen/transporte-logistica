package com.transporte.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporte.entity.Usuario;
import com.transporte.exception.NegocioException;
import com.transporte.repository.UsuarioRepository;

/**
 * El login (POST /api/auth/login) y el logout (POST /api/auth/logout) los resuelve Spring Security,
 * ver SecurityConfig. Acá solo se responde "quién está logueado".
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> quienSoy(Authentication auth) {
        Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> NegocioException.prohibido("Sesión inválida"));
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("username", usuario.getUsername());
        datos.put("nombre", usuario.getNombre() + " " + usuario.getApellido());
        datos.put("rol", usuario.getRol());
        return datos;
    }
}
