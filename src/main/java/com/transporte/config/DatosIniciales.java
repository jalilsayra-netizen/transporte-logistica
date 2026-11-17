package com.transporte.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.transporte.entity.*;
import com.transporte.repository.*;

/**
 * Se ejecuta al arrancar. Si la base está vacía, carga los datos de arranque:
 * estados de envío, tipos de vehículo, usuarios de prueba y algunos datos de ejemplo.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final EstadoEnvioRepository estadoRepository;
    private final TipoVehiculoRepository tipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(EstadoEnvioRepository estadoRepository, TipoVehiculoRepository tipoRepository,
                          UsuarioRepository usuarioRepository, ConductorRepository conductorRepository,
                          VehiculoRepository vehiculoRepository, ClienteRepository clienteRepository,
                          PedidoRepository pedidoRepository, PasswordEncoder passwordEncoder) {
        this.estadoRepository = estadoRepository;
        this.tipoRepository = tipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // ya hay datos cargados: no se vuelve a hacer nada
        }

        for (String nombre : new String[] {"PREPARANDO", "EN_TRANSITO", "ENTREGADO", "CANCELADO"}) {
            EstadoEnvio estado = new EstadoEnvio();
            estado.setNombre(nombre);
            estadoRepository.save(estado);
        }

        TipoVehiculo camion = crearTipo("Camión");
        TipoVehiculo camioneta = crearTipo("Camioneta");
        TipoVehiculo furgon = crearTipo("Furgón");
        crearTipo("Utilitario");
        crearTipo("Acoplado");

        Conductor gomez = crearConductor("30123456", "Gómez", "Carlos", "LIC-0001", "381-4111111", "carlos.gomez@transporte.com");
        Conductor perez = crearConductor("28987654", "Pérez", "Marta", "LIC-0002", "381-4222222", "marta.perez@transporte.com");

        crearVehiculo("AB123CD", "Mercedes-Benz", "Atego", 2019, camion, 8000.0, "DISPONIBLE");
        crearVehiculo("AC456DE", "Toyota", "Hilux", 2021, camioneta, 1000.0, "DISPONIBLE");
        crearVehiculo("AD789FG", "Renault", "Master", 2018, furgon, 1500.0, "EN_MANTENIMIENTO"); // para probar RN01

        Cliente norte = crearCliente("Distribuidora Norte S.R.L.", "30-71234567-8", "381-4333333", "compras@norte.com", "Av. Mate de Luna 1500", "San Miguel de Tucumán");
        Cliente tornillo = crearCliente("Ferretería El Tornillo", "20-27654321-5", "381-4444444", "info@eltornillo.com", "Calle Lavalle 300", "Yerba Buena");

        crearUsuario("admin", "Administrador", "Sistema", "admin123", "ADMIN", null);
        crearUsuario("operador", "Olga", "Operadora", "operador123", "OPERADOR", null);
        crearUsuario("conductor", gomez.getNombre(), gomez.getApellido(), "conductor123", "CONDUCTOR", gomez);
        crearUsuario("conductor2", perez.getNombre(), perez.getApellido(), "conductor123", "CONDUCTOR", perez);

        crearPedido(norte, "Av. Mate de Luna 1500, San Miguel de Tucumán", "Av. Belgrano 800, Salta", "Bebidas", 40, 2500.0);
        crearPedido(tornillo, "Calle Lavalle 300, Yerba Buena", "Ruta 9 km 1300, Tafí Viejo", "Materiales de ferretería", 15, 600.0);
        crearPedido(norte, "Av. Mate de Luna 1500, San Miguel de Tucumán", "Av. Sarmiento 450, Santiago del Estero", "Alimentos", 100, 9500.0);
    }

    private TipoVehiculo crearTipo(String nombre) {
        TipoVehiculo tipo = new TipoVehiculo();
        tipo.setNombre(nombre);
        return tipoRepository.save(tipo);
    }

    private Conductor crearConductor(String dni, String apellido, String nombre, String licencia, String telefono, String email) {
        Conductor c = new Conductor();
        c.setDni(dni);
        c.setApellido(apellido);
        c.setNombre(nombre);
        c.setLicencia(licencia);
        c.setVencimientoLicencia(LocalDate.now().plusYears(2));
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setEstado("ACTIVO");
        return conductorRepository.save(c);
    }

    private void crearVehiculo(String patente, String marca, String modelo, int anio, TipoVehiculo tipo, double capacidad, String estado) {
        Vehiculo v = new Vehiculo();
        v.setPatente(patente);
        v.setMarca(marca);
        v.setModelo(modelo);
        v.setAnio(anio);
        v.setTipoVehiculo(tipo);
        v.setCapacidad(capacidad);
        v.setEstado(estado);
        vehiculoRepository.save(v);
    }

    private Cliente crearCliente(String razonSocial, String cuit, String telefono, String email, String domicilio, String localidad) {
        Cliente c = new Cliente();
        c.setRazonSocial(razonSocial);
        c.setCuit(cuit);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setDomicilio(domicilio);
        c.setLocalidad(localidad);
        c.setEstado("ACTIVO");
        return clienteRepository.save(c);
    }

    private void crearUsuario(String username, String nombre, String apellido, String password, String rol, Conductor conductor) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setEstado("ACTIVO");
        u.setConductor(conductor);
        usuarioRepository.save(u);
    }

    private void crearPedido(Cliente cliente, String origen, String destino, String carga, int cantidad, double peso) {
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setOrigen(origen);
        p.setDestino(destino);
        p.setTipoCarga(carga);
        p.setCantidad(cantidad);
        p.setPeso(peso);
        p.setFechaEstimadaEntrega(LocalDate.now().plusDays(3));
        pedidoRepository.save(p);
    }
}
