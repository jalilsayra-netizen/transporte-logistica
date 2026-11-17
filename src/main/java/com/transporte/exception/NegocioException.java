package com.transporte.exception;

import org.springframework.http.HttpStatus;

/**
 * Una sola excepción para todos los errores "esperables" del sistema:
 * datos inválidos, reglas de negocio, registros que no existen, falta de permisos.
 * El ManejadorErrores la convierte en un JSON {"error": "mensaje"} con el código HTTP correspondiente.
 */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    /** Error común (código 400). Ejemplo: throw new NegocioException("Capacidad de carga superada"); */
    public NegocioException(String mensaje) {
        super(mensaje);
        this.status = HttpStatus.BAD_REQUEST;
    }

    private NegocioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    /** El registro no existe (código 404). Ejemplo: throw NegocioException.noEncontrado("Cliente"); */
    public static NegocioException noEncontrado(String que) {
        return new NegocioException(que + " no encontrado", HttpStatus.NOT_FOUND);
    }

    /** El usuario no tiene permiso para esta operación (código 403). */
    public static NegocioException prohibido(String mensaje) {
        return new NegocioException(mensaje, HttpStatus.FORBIDDEN);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
