package com.transporte.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Seguridad del sistema:
 *  - AUTENTICACIÓN: quién sos (usuario y contraseña).
 *  - AUTORIZACIÓN: qué podés hacer según tu rol (ADMIN, OPERADOR o CONDUCTOR).
 * Los permisos se controlan acá, en el BACKEND. Ocultar un botón en la pantalla no alcanza.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Para guardar y comparar contraseñas con BCrypt (hash, no texto plano)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // se desactiva para simplificar el trabajo práctico
            .authorizeHttpRequests(auth -> auth
                // Páginas, estilos y scripts: abiertos (los datos están protegidos en la API)
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                // Login
                .requestMatchers("/api/auth/login").permitAll()
                // Cualquier usuario logueado
                .requestMatchers("/api/auth/**", "/api/dashboard").authenticated()
                // Documentación Swagger: solo ADMIN
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                // Usuarios: solo ADMIN
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                // Clientes y pedidos: ADMIN y OPERADOR
                .requestMatchers("/api/clientes/**", "/api/pedidos/**").hasAnyRole("ADMIN", "OPERADOR")
                // Vehículos, conductores y tipos: el OPERADOR solo puede consultarlos (GET)
                .requestMatchers(HttpMethod.GET, "/api/vehiculos/**", "/api/conductores/**", "/api/tipos-vehiculo/**")
                    .hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers("/api/vehiculos/**").hasAnyRole("ADMIN", "OPERADOR")
                .requestMatchers("/api/conductores/**", "/api/tipos-vehiculo/**").hasRole("ADMIN")
                // Envíos: consultar lo ven los 3 roles; la entrega es de ADMIN y CONDUCTOR; el resto ADMIN y OPERADOR
                .requestMatchers(HttpMethod.GET, "/api/envios", "/api/envios/*", "/api/envios/*/historial").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/envios/*/estado").hasAnyRole("ADMIN", "OPERADOR", "CONDUCTOR")
                .requestMatchers(HttpMethod.POST, "/api/envios/*/entrega").hasAnyRole("ADMIN", "CONDUCTOR")
                .requestMatchers("/api/envios/**").hasAnyRole("ADMIN", "OPERADOR")
                // Entregas: ADMIN y CONDUCTOR
                .requestMatchers("/api/entregas/**").hasAnyRole("ADMIN", "CONDUCTOR")
                // Todo lo demás, prohibido
                .anyRequest().denyAll())
            // Login: POST /api/auth/login con "username" y "password". Responde un JSON (no redirige).
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> responder(res, mapper, 200, Map.of("username", auth.getName())))
                .failureHandler((req, res, ex) -> responder(res, mapper, 401, Map.of("error", "Usuario o contraseña incorrectos"))))
            // Logout: POST /api/auth/logout
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(200)))
            // Sin sesión -> 401. Con sesión pero sin permiso -> 403. Siempre en JSON.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> responder(res, mapper, 401, Map.of("error", "Debe iniciar sesión")))
                .accessDeniedHandler((req, res, e) -> responder(res, mapper, 403, Map.of("error", "No tiene permisos para realizar esta operación"))));
        return http.build();
    }

    // Escribe un JSON como respuesta
    private void responder(HttpServletResponse res, ObjectMapper mapper, int codigo, Map<String, Object> cuerpo)
            throws IOException {
        res.setStatus(codigo);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getWriter(), cuerpo);
    }
}
