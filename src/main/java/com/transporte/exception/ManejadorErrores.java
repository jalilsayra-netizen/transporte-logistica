package com.transporte.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Atrapa los errores de TODOS los controllers y responde siempre con un JSON simple:
 * {"error": "mensaje"}. Así el usuario nunca ve el error técnico de Java.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    private ResponseEntity<Map<String, Object>> responder(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", mensaje);
        return ResponseEntity.status(status).body(cuerpo);
    }

    // Errores que lanzamos nosotros (reglas de negocio, no encontrado, sin permiso)
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(NegocioException ex) {
        return responder(ex.getStatus(), ex.getMessage());
    }

    // Falló alguna validación de la entidad (@NotBlank, @Size, @Pattern...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "Datos incorrectos");
        cuerpo.put("campos", campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    // El JSON llegó mal armado (por ejemplo una fecha con formato inválido)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Datos incorrectos: algún valor tiene un formato no válido");
    }

    // Lo rechazó la base de datos (por ejemplo un dato único repetido)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> baseDeDatos(DataIntegrityViolationException ex) {
        return responder(HttpStatus.BAD_REQUEST, "No se pudo guardar: el dato ya existe o está en uso");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> noExiste(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    // Cualquier otro error inesperado: se anota en el log y al usuario se le muestra un mensaje genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> otro(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}
