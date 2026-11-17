package com.transporte.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporte.entity.Conductor;
import com.transporte.entity.Entrega;
import com.transporte.entity.Envio;
import com.transporte.entity.EstadoEnvio;
import com.transporte.entity.HistorialEstado;
import com.transporte.entity.Pedido;
import com.transporte.entity.Usuario;
import com.transporte.entity.Vehiculo;
import com.transporte.exception.NegocioException;
import com.transporte.repository.ConductorRepository;
import com.transporte.repository.EntregaRepository;
import com.transporte.repository.EnvioRepository;
import com.transporte.repository.EstadoEnvioRepository;
import com.transporte.repository.HistorialEstadoRepository;
import com.transporte.repository.PedidoRepository;
import com.transporte.repository.UsuarioRepository;
import com.transporte.repository.VehiculoRepository;

/**
 * Lógica de los envíos. Acá están las reglas de negocio RN01 a RN07 del trabajo práctico.
 *
 * Estados de un envío:  PREPARANDO -> EN_TRANSITO -> ENTREGADO
 *                       (y desde PREPARANDO o EN_TRANSITO se puede pasar a CANCELADO)
 */
@Service
@Transactional
public class EnvioService {

    private static final String PREPARANDO = "PREPARANDO";
    private static final String EN_TRANSITO = "EN_TRANSITO";
    private static final String ENTREGADO = "ENTREGADO";
    private static final String CANCELADO = "CANCELADO";

    private final EnvioRepository envioRepository;
    private final PedidoRepository pedidoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final EstadoEnvioRepository estadoRepository;
    private final HistorialEstadoRepository historialRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioService(EnvioRepository envioRepository, PedidoRepository pedidoRepository,
                        VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository,
                        EstadoEnvioRepository estadoRepository, HistorialEstadoRepository historialRepository,
                        EntregaRepository entregaRepository, UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.pedidoRepository = pedidoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.estadoRepository = estadoRepository;
        this.historialRepository = historialRepository;
        this.entregaRepository = entregaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ================================================================ CONSULTAS

    // El conductor solo ve SUS envíos; ADMIN y OPERADOR ven todos
    public List<Envio> listar(String estado, Authentication auth) {
        boolean hayFiltro = estado != null && !estado.isBlank();
        if (esConductor(auth)) {
            Long idConductor = conductorLogueado(auth).getIdConductor();
            if (hayFiltro) {
                return envioRepository.findByConductorIdConductorAndEstadoNombreOrderByIdEnvioDesc(idConductor, estado);
            }
            return envioRepository.findByConductorIdConductorOrderByIdEnvioDesc(idConductor);
        }
        if (hayFiltro) {
            return envioRepository.findByEstadoNombreOrderByIdEnvioDesc(estado);
        }
        return envioRepository.findAllByOrderByIdEnvioDesc();
    }

    public Envio buscarPorId(Long id, Authentication auth) {
        Envio envio = buscar(id);
        controlarAcceso(envio, auth);
        return envio;
    }

    public List<HistorialEstado> historial(Long id, Authentication auth) {
        controlarAcceso(buscar(id), auth);
        return historialRepository.findByEnvioIdEnvioOrderByFechaAscIdHistorialAsc(id);
    }

    public List<Entrega> listarEntregas(Authentication auth) {
        if (esConductor(auth)) {
            return entregaRepository.findByEnvioConductorIdConductorOrderByIdEntregaDesc(conductorLogueado(auth).getIdConductor());
        }
        return entregaRepository.findAllByOrderByIdEntregaDesc();
    }

    public Entrega buscarEntrega(Long id, Authentication auth) {
        Entrega entrega = entregaRepository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Entrega"));
        controlarAcceso(entrega.getEnvio(), auth);
        return entrega;
    }

    // ================================================================ ALTA Y EDICIÓN

    public Envio crear(Envio datos, Authentication auth) {
        // 1) el pedido tiene que existir y no tener ya un envío
        Pedido pedido = pedidoRepository.findById(datos.getPedido().getIdPedido())
                .orElseThrow(() -> NegocioException.noEncontrado("Pedido"));
        if (envioRepository.existsByPedidoIdPedido(pedido.getIdPedido())) {
            throw new NegocioException("El pedido ya tiene un envío asociado");
        }
        // 2) las fechas tienen que ser coherentes
        validarFechas(datos.getFechaSalida(), datos.getFechaEstimadaLlegada(), true);

        // 3) se crea el envío en estado PREPARANDO
        Envio envio = new Envio();
        envio.setPedido(pedido);
        envio.setFechaSalida(datos.getFechaSalida());
        envio.setFechaEstimadaLlegada(datos.getFechaEstimadaLlegada());
        envio.setItinerario(datos.getItinerario().trim());
        envio.setObservacionesRuta(datos.getObservacionesRuta());
        envio.setEstado(buscarEstado(PREPARANDO));
        envio = envioRepository.save(envio);

        // 4) vehículo y conductor son opcionales al crear. Si alguna regla falla se cancela todo (rollback).
        if (datos.getVehiculo() != null && datos.getVehiculo().getIdVehiculo() != null) {
            ponerVehiculo(envio, datos.getVehiculo().getIdVehiculo());
        }
        if (datos.getConductor() != null && datos.getConductor().getIdConductor() != null) {
            ponerConductor(envio, datos.getConductor().getIdConductor());
        }
        guardarHistorial(envio, "Envío creado", auth); // RN07
        return envio;
    }

    public Envio actualizar(Long id, Envio datos, Authentication auth) {
        Envio envio = buscar(id);
        controlarNoFinalizado(envio);
        validarFechas(datos.getFechaSalida(), datos.getFechaEstimadaLlegada(), false);
        envio.setFechaSalida(datos.getFechaSalida());
        envio.setFechaEstimadaLlegada(datos.getFechaEstimadaLlegada());
        envio.setItinerario(datos.getItinerario().trim());
        envio.setObservacionesRuta(datos.getObservacionesRuta());
        // con las fechas nuevas se vuelven a controlar el vehículo y el conductor ya asignados
        if (envio.getVehiculo() != null) {
            ponerVehiculo(envio, envio.getVehiculo().getIdVehiculo());
        }
        if (envio.getConductor() != null) {
            ponerConductor(envio, envio.getConductor().getIdConductor());
        }
        return envio;
    }

    public Envio asignarVehiculo(Long idEnvio, Long idVehiculo) {
        Envio envio = buscar(idEnvio);
        controlarSePuedeAsignar(envio);
        ponerVehiculo(envio, idVehiculo);
        return envio;
    }

    public Envio asignarConductor(Long idEnvio, Long idConductor) {
        Envio envio = buscar(idEnvio);
        controlarSePuedeAsignar(envio);
        ponerConductor(envio, idConductor);
        return envio;
    }

    // DELETE de un envío: nunca se borra (se pierde el historial), se CANCELA
    public void cancelar(Long id, Authentication auth) {
        cambiarEstado(id, CANCELADO, "Envío cancelado", auth);
    }

    // ================================================================ CAMBIO DE ESTADO

    public Envio cambiarEstado(Long id, String nuevo, String observaciones, Authentication auth) {
        Envio envio = buscar(id);
        controlarAcceso(envio, auth);

        if (nuevo == null || !List.of(PREPARANDO, EN_TRANSITO, ENTREGADO, CANCELADO).contains(nuevo.trim().toUpperCase())) {
            throw new NegocioException("Estado inválido. Valores permitidos: PREPARANDO, EN_TRANSITO, ENTREGADO, CANCELADO");
        }
        nuevo = nuevo.trim().toUpperCase();
        String actual = envio.getEstado().getNombre();

        // RN04: un envío ENTREGADO o CANCELADO ya no puede cambiar
        if (actual.equals(ENTREGADO) || actual.equals(CANCELADO)) {
            throw new NegocioException("Operación no permitida: el envío ya se encuentra finalizado");
        }
        if (nuevo.equals(actual)) {
            throw new NegocioException("El envío ya se encuentra en estado " + actual);
        }
        // El conductor solo puede iniciar el traslado de sus envíos (PREPARANDO -> EN_TRANSITO)
        if (esConductor(auth) && !(actual.equals(PREPARANDO) && nuevo.equals(EN_TRANSITO))) {
            throw NegocioException.prohibido("Operación no autorizada");
        }
        // RN06: para ENTREGADO hay que usar "registrar entrega" porque pide datos obligatorios
        if (nuevo.equals(ENTREGADO)) {
            throw new NegocioException("Para marcar el envío como entregado debe registrar la entrega "
                    + "(fecha, hora, persona que recibe y observaciones)");
        }
        // Cambios permitidos: PREPARANDO -> EN_TRANSITO o CANCELADO; EN_TRANSITO -> CANCELADO
        boolean permitido = (actual.equals(PREPARANDO) && (nuevo.equals(EN_TRANSITO) || nuevo.equals(CANCELADO)))
                || (actual.equals(EN_TRANSITO) && nuevo.equals(CANCELADO));
        if (!permitido) {
            throw new NegocioException("Transición de estado no permitida: " + actual + " -> " + nuevo);
        }

        if (nuevo.equals(EN_TRANSITO)) {
            if (envio.getVehiculo() == null || envio.getConductor() == null) {
                throw new NegocioException("Debe asignar vehículo y conductor antes de iniciar el traslado");
            }
            String estadoVehiculo = envio.getVehiculo().getEstado();
            if (estadoVehiculo.equals("EN_MANTENIMIENTO") || estadoVehiculo.equals("INACTIVO")) {
                throw new NegocioException("Vehículo no disponible para asignación"); // RN01
            }
            envio.getVehiculo().setEstado("EN_TRANSITO");
        } else {
            liberarVehiculo(envio);
        }
        envio.setEstado(buscarEstado(nuevo));
        guardarHistorial(envio, observaciones, auth); // RN07
        return envio;
    }

    // ================================================================ ENTREGA

    public Entrega registrarEntrega(Long id, Entrega datos, Authentication auth) {
        Envio envio = buscar(id);
        controlarAcceso(envio, auth);
        String actual = envio.getEstado().getNombre();

        if (actual.equals(ENTREGADO) || actual.equals(CANCELADO)) {
            throw new NegocioException("El envío ya se encuentra finalizado y no se puede modificar");
        }
        if (!actual.equals(EN_TRANSITO)) {
            throw new NegocioException("El envío debe estar EN_TRANSITO para registrar la entrega");
        }
        // RN06: fecha, hora, persona que recibe y observaciones son obligatorias
        if (datos.getFecha() == null || datos.getHora() == null || vacio(datos.getPersonaRecibe())
                || vacio(datos.getObservaciones())) {
            throw new NegocioException("Debe completar todos los datos requeridos para confirmar la entrega");
        }
        if (datos.getFecha().isAfter(LocalDate.now())) {
            throw new NegocioException("Datos incorrectos: la fecha de entrega no puede ser futura");
        }
        if (!vacio(datos.getDniRecibe()) && !datos.getDniRecibe().trim().matches("^\\d{7,8}$")) {
            throw new NegocioException("Datos incorrectos: el DNI de quien recibe debe tener 7 u 8 dígitos");
        }
        if (datos.getPersonaRecibe().trim().length() > 100 || datos.getObservaciones().trim().length() > 255) {
            throw new NegocioException("Datos incorrectos: algún texto supera la longitud permitida");
        }
        if (!vacio(datos.getEstadoEntrega()) && !List.of("CONFORME", "CON_OBSERVACIONES").contains(datos.getEstadoEntrega())) {
            throw new NegocioException("Datos incorrectos: el estado de la entrega debe ser CONFORME o CON_OBSERVACIONES");
        }

        Entrega entrega = new Entrega();
        entrega.setEnvio(envio);
        entrega.setFecha(datos.getFecha());
        entrega.setHora(datos.getHora());
        entrega.setPersonaRecibe(datos.getPersonaRecibe().trim());
        entrega.setDniRecibe(vacio(datos.getDniRecibe()) ? null : datos.getDniRecibe().trim());
        entrega.setObservaciones(datos.getObservaciones().trim());
        entrega.setEstadoEntrega(vacio(datos.getEstadoEntrega()) ? "CONFORME" : datos.getEstadoEntrega());
        entrega.setComprobante(vacio(datos.getComprobante()) ? null : datos.getComprobante().trim());
        entrega = entregaRepository.save(entrega);

        liberarVehiculo(envio);
        envio.setEstado(buscarEstado(ENTREGADO));
        guardarHistorial(envio, "Entrega registrada. Recibió: " + entrega.getPersonaRecibe(), auth); // RN07
        return entrega;
    }

    // ================================================================ REGLAS DE ASIGNACIÓN

    // RN01: no asignar un vehículo en mantenimiento. RN05: la carga tiene que entrar. RN03: sin viajes superpuestos.
    private void ponerVehiculo(Envio envio, Long idVehiculo) {
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> NegocioException.noEncontrado("Vehículo"));
        if (vehiculo.getEstado().equals("EN_MANTENIMIENTO") || vehiculo.getEstado().equals("INACTIVO")) {
            throw new NegocioException("Vehículo no disponible para asignación");
        }
        if (envio.getPedido().getPeso() > vehiculo.getCapacidad()) {
            throw new NegocioException("Capacidad de carga superada");
        }
        long superpuestos = envioRepository.contarSolapadosVehiculo(idVehiculo, envio.getIdEnvio(),
                envio.getFechaSalida(), envio.getFechaEstimadaLlegada());
        if (superpuestos > 0) {
            throw new NegocioException("El vehículo ya tiene un viaje asignado en ese horario");
        }
        envio.setVehiculo(vehiculo);
    }

    // RN02: el conductor tiene que estar activo y con la licencia vigente. RN03: sin viajes superpuestos.
    private void ponerConductor(Envio envio, Long idConductor) {
        Conductor conductor = conductorRepository.findById(idConductor)
                .orElseThrow(() -> NegocioException.noEncontrado("Conductor"));
        if (!conductor.getEstado().equals("ACTIVO")) {
            throw new NegocioException("Conductor no habilitado");
        }
        if (conductor.getVencimientoLicencia().isBefore(envio.getFechaEstimadaLlegada().toLocalDate())) {
            throw new NegocioException("Conductor no habilitado: la licencia está vencida o vence antes de la fecha de llegada");
        }
        long superpuestos = envioRepository.contarSolapadosConductor(idConductor, envio.getIdEnvio(),
                envio.getFechaSalida(), envio.getFechaEstimadaLlegada());
        if (superpuestos > 0) {
            throw new NegocioException("El conductor ya se encuentra asignado a otra ruta activa");
        }
        envio.setConductor(conductor);
    }

    // ================================================================ AUXILIARES

    private void validarFechas(LocalDateTime salida, LocalDateTime llegada, boolean esNuevo) {
        if (!llegada.isAfter(salida)) {
            throw new NegocioException("Datos incorrectos: la fecha estimada de llegada debe ser posterior a la de salida");
        }
        if (esNuevo && salida.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
            throw new NegocioException("Datos incorrectos: la fecha programada de salida es anterior a la fecha actual");
        }
    }

    // RN07: cada cambio de estado deja un registro que no se puede modificar
    private void guardarHistorial(Envio envio, String observaciones, Authentication auth) {
        HistorialEstado historial = new HistorialEstado();
        historial.setEnvio(envio);
        historial.setEstado(envio.getEstado());
        historial.setFecha(LocalDateTime.now());
        historial.setObservaciones(vacio(observaciones) ? null : observaciones.trim());
        historial.setUsuario(auth.getName());
        historialRepository.save(historial);
    }

    // Cuando el envío termina (entregado o cancelado) el vehículo queda disponible otra vez
    private void liberarVehiculo(Envio envio) {
        Vehiculo vehiculo = envio.getVehiculo();
        if (vehiculo != null && vehiculo.getEstado().equals("EN_TRANSITO")) {
            vehiculo.setEstado("DISPONIBLE");
        }
    }

    private void controlarNoFinalizado(Envio envio) {
        String estado = envio.getEstado().getNombre();
        if (estado.equals(ENTREGADO) || estado.equals(CANCELADO)) {
            throw new NegocioException("El envío ya se encuentra finalizado y no se puede modificar");
        }
    }

    private void controlarSePuedeAsignar(Envio envio) {
        controlarNoFinalizado(envio);
        if (envio.getEstado().getNombre().equals(EN_TRANSITO)) {
            throw new NegocioException("No se puede reasignar un envío que ya está en tránsito");
        }
    }

    private Envio buscar(Long id) {
        return envioRepository.findById(id).orElseThrow(() -> NegocioException.noEncontrado("Envío"));
    }

    private EstadoEnvio buscarEstado(String nombre) {
        return estadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + nombre + " en la base de datos"));
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean esConductor(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONDUCTOR"));
    }

    // Busca el conductor que corresponde al usuario que inició sesión
    private Conductor conductorLogueado(Authentication auth) {
        Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> NegocioException.prohibido("Operación no autorizada"));
        if (usuario.getConductor() == null) {
            throw NegocioException.prohibido("Su usuario no está asociado a un conductor");
        }
        return usuario.getConductor();
    }

    // Un conductor solo puede trabajar con los envíos que tiene asignados
    private void controlarAcceso(Envio envio, Authentication auth) {
        if (esConductor(auth)) {
            Conductor conductor = conductorLogueado(auth);
            if (envio.getConductor() == null || !envio.getConductor().getIdConductor().equals(conductor.getIdConductor())) {
                throw NegocioException.prohibido("Operación no autorizada");
            }
        }
    }
}
