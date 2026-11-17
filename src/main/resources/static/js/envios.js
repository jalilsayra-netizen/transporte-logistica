/* envios.js - Pantalla Envíos: listado, alta de envío, asignación y cambio de estado */
(async () => {
    const usuario = await iniciarPagina(['ADMIN', 'OPERADOR', 'CONDUCTOR']);
    if (!usuario) return;

    const puedeGestionar = usuario.rol === 'ADMIN' || usuario.rol === 'OPERADOR';
    const esConductor = usuario.rol === 'CONDUCTOR';
    const puedeEntregar = usuario.rol === 'ADMIN' || usuario.rol === 'CONDUCTOR';

    const tbody = document.querySelector('#tablaEnvios tbody');
    const filtro = document.getElementById('filtroEstado');
    const btnNuevo = document.getElementById('btnNuevoEnvio');
    const modalEnvio = new bootstrap.Modal(document.getElementById('modalEnvio'));
    const modalAsignar = new bootstrap.Modal(document.getElementById('modalAsignar'));
    const modalEstado = new bootstrap.Modal(document.getElementById('modalEstado'));
    const formEnvio = document.getElementById('formEnvio');
    const formAsignar = document.getElementById('formAsignar');
    const formEstado = document.getElementById('formEstado');

    let envios = [];
    let envioActual = null; // envío sobre el que se abrió un modal

    if (puedeGestionar) btnNuevo.classList.remove('d-none');

    /* ------------------------------------------------------------ listado */

    async function cargar() {
        try {
            envios = await api('/api/envios' + (filtro.value ? '?estado=' + filtro.value : ''));
            dibujar();
        } catch (e) { mostrarError(e); }
    }

    function acciones(e) {
        const estado = e.estado.nombre;
        const finalizado = estado === 'ENTREGADO' || estado === 'CANCELADO';
        let h = `<a class="btn btn-sm btn-outline-secondary" href="seguimiento.html?id=${e.idEnvio}">Seguimiento</a>`;
        if (puedeGestionar && !finalizado) {
            h += ` <button class="btn btn-sm btn-outline-primary" data-accion="asignar" data-id="${e.idEnvio}">Asignar</button>`;
            h += ` <button class="btn btn-sm btn-outline-dark" data-accion="estado" data-id="${e.idEnvio}">Estado</button>`;
        }
        if (esConductor && estado === 'PREPARANDO') {
            h += ` <button class="btn btn-sm btn-primary" data-accion="iniciar" data-id="${e.idEnvio}">Iniciar traslado</button>`;
        }
        if (puedeEntregar && estado === 'EN_TRANSITO') {
            h += ` <a class="btn btn-sm btn-success" href="entregas.html?envio=${e.idEnvio}">Registrar entrega</a>`;
        }
        return h;
    }

    function dibujar() {
        if (!envios.length) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted py-4">No hay envíos para mostrar</td></tr>';
            return;
        }
        const sin = '<span class="text-muted">Sin asignar</span>';
        tbody.innerHTML = envios.map(e => `<tr>
            <td><strong>#${e.idEnvio}</strong></td>
            <td>${esc(e.pedido.cliente.razonSocial)}</td>
            <td>${esc(e.pedido.origen)}<br><small class="text-muted">&rarr; ${esc(e.pedido.destino)}</small></td>
            <td>${e.vehiculo ? esc(e.vehiculo.patente) : sin}</td>
            <td>${e.conductor ? esc(e.conductor.apellido) + ', ' + esc(e.conductor.nombre) : sin}</td>
            <td>${esc(formatearFechaHora(e.fechaSalida))}</td>
            <td>${esc(formatearFechaHora(e.fechaEstimadaLlegada))}</td>
            <td>${etiquetaEstado(e.estado.nombre)}</td>
            <td class="text-end text-nowrap">${acciones(e)}</td></tr>`).join('');
    }

    filtro.addEventListener('change', cargar);

    /* ------------------------------------------------------------ nuevo envío */

    async function cargarVehiculosYConductores(selVehiculo, selConductor, vehiculoActual, conductorActual) {
        const [vehiculos, conductores] = await Promise.all([
            api('/api/vehiculos?estado=DISPONIBLE'), api('/api/conductores?estado=ACTIVO')]);
        selVehiculo.innerHTML = '<option value="">Sin asignar</option>' + vehiculos.map(v =>
            `<option value="${v.idVehiculo}">${esc(v.patente)} - ${esc(v.marca)} ${esc(v.modelo)} (${esc(v.capacidad)} kg)</option>`).join('');
        selConductor.innerHTML = '<option value="">Sin asignar</option>' + conductores.map(c =>
            `<option value="${c.idConductor}">${esc(c.apellido)}, ${esc(c.nombre)} (DNI ${esc(c.dni)})</option>`).join('');
        if (vehiculoActual) selVehiculo.value = vehiculoActual;
        if (conductorActual) selConductor.value = conductorActual;
    }

    btnNuevo.addEventListener('click', async () => {
        formEnvio.reset();
        formEnvio.classList.remove('was-validated');
        document.getElementById('alertasEnvio').innerHTML = '';
        try {
            const pedidos = await api('/api/pedidos?pendientes=true');
            const sel = document.getElementById('f_pedido_idPedido');
            sel.innerHTML = '<option value="">Seleccione un pedido...</option>' + pedidos.map(p =>
                `<option value="${p.idPedido}">#${p.idPedido} - ${esc(p.cliente.razonSocial)}: ${esc(p.origen)} &rarr; ${esc(p.destino)} (${esc(p.peso)} kg)</option>`).join('');
            await cargarVehiculosYConductores(document.getElementById('f_vehiculo_idVehiculo'), document.getElementById('f_conductor_idConductor'));
            if (!pedidos.length) {
                mostrarAlerta('warning', 'No hay pedidos pendientes. Primero registre un pedido en la pantalla Pedidos.', 'alertasEnvio');
            }
        } catch (e) { mostrarError(e, 'alertasEnvio'); }
        // Valores sugeridos: salida dentro de 1 hora, llegada 24 horas después
        const salida = new Date(Date.now() + 60 * 60 * 1000);
        const llegada = new Date(salida.getTime() + 24 * 60 * 60 * 1000);
        formEnvio.fechaSalida.min = valorInputFechaHora(new Date());
        formEnvio.fechaSalida.value = valorInputFechaHora(salida);
        formEnvio.fechaEstimadaLlegada.value = valorInputFechaHora(llegada);
        modalEnvio.show();
    });

    formEnvio.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        formEnvio.classList.add('was-validated');
        if (!formEnvio.checkValidity()) return;
        if (formEnvio.fechaEstimadaLlegada.value <= formEnvio.fechaSalida.value) {
            mostrarAlerta('danger', '<strong>La fecha estimada de llegada debe ser posterior a la de salida</strong>', 'alertasEnvio');
            return;
        }
        try {
            const creado = await api('/api/envios', { metodo: 'POST', cuerpo: leerFormulario(formEnvio) });
            modalEnvio.hide();
            mostrarAlerta('success', `Envío #${creado.idEnvio} creado correctamente`);
            cargar();
        } catch (e) { mostrarError(e, 'alertasEnvio'); }
    });

    /* ------------------------------------------------------------ acciones por fila */

    tbody.addEventListener('click', async (ev) => {
        const boton = ev.target.closest('button[data-accion]');
        if (!boton) return;
        envioActual = envios.find(e => String(e.idEnvio) === boton.dataset.id);
        if (!envioActual) return;

        if (boton.dataset.accion === 'asignar') abrirAsignar();
        else if (boton.dataset.accion === 'estado') abrirEstado();
        else if (boton.dataset.accion === 'iniciar') iniciarTraslado();
    });

    /* ---- asignar vehículo / conductor ---- */
    async function abrirAsignar() {
        formAsignar.reset();
        document.getElementById('alertasAsignar').innerHTML = '';
        document.getElementById('resumenAsignar').textContent =
            `Envío #${envioActual.idEnvio} - carga de ${envioActual.pedido.peso} kg`;
        try {
            await cargarVehiculosYConductores(document.getElementById('asig_vehiculo'), document.getElementById('asig_conductor'),
                envioActual.vehiculo && envioActual.vehiculo.idVehiculo, envioActual.conductor && envioActual.conductor.idConductor);
        } catch (e) { mostrarError(e, 'alertasAsignar'); }
        modalAsignar.show();
    }

    formAsignar.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const idVehiculo = document.getElementById('asig_vehiculo').value;
        const idConductor = document.getElementById('asig_conductor').value;
        if (!idVehiculo && !idConductor) {
            mostrarAlerta('warning', 'Seleccione un vehículo o un conductor', 'alertasAsignar');
            return;
        }
        try {
            // Son dos llamadas distintas a la API: una asigna el vehículo y la otra el conductor
            if (idVehiculo) await api(`/api/envios/${envioActual.idEnvio}/vehiculo/${idVehiculo}`, { metodo: 'PUT' });
            if (idConductor) await api(`/api/envios/${envioActual.idEnvio}/conductor/${idConductor}`, { metodo: 'PUT' });
            modalAsignar.hide();
            mostrarAlerta('success', 'Asignación guardada correctamente');
            cargar();
        } catch (e) { mostrarError(e, 'alertasAsignar'); }
    });

    /* ---- cambiar estado ---- */
    function abrirEstado() {
        formEstado.reset();
        formEstado.classList.remove('was-validated');
        document.getElementById('alertasEstado').innerHTML = '';
        const actual = envioActual.estado.nombre;
        document.getElementById('resumenEstado').innerHTML =
            `Envío #${envioActual.idEnvio} - estado actual: ${etiquetaEstado(actual)}`;
        // Solo se ofrecen las transiciones permitidas
        const permitidos = actual === 'PREPARANDO' ? ['EN_TRANSITO', 'CANCELADO'] : ['CANCELADO'];
        document.getElementById('est_estado').innerHTML = '<option value="">Seleccione...</option>' +
            permitidos.map(s => `<option value="${s}">${s.replace('_', ' ')}</option>`).join('');
        modalEstado.show();
    }

    formEstado.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        formEstado.classList.add('was-validated');
        if (!formEstado.checkValidity()) return;
        try {
            await api(`/api/envios/${envioActual.idEnvio}/estado`, { metodo: 'PUT', cuerpo: leerFormulario(formEstado) });
            modalEstado.hide();
            mostrarAlerta('success', 'Estado actualizado correctamente');
            cargar();
        } catch (e) { mostrarError(e, 'alertasEstado'); }
    });

    /* ---- el conductor inicia el traslado de su envío ---- */
    async function iniciarTraslado() {
        if (!confirm(`¿Iniciar el traslado del envío #${envioActual.idEnvio}?`)) return;
        try {
            await api(`/api/envios/${envioActual.idEnvio}/estado`,
                { metodo: 'PUT', cuerpo: { estado: 'EN_TRANSITO', observaciones: 'Traslado iniciado por el conductor' } });
            mostrarAlerta('success', 'Traslado iniciado. El envío está EN TRÁNSITO');
            cargar();
        } catch (e) { mostrarError(e); }
    }

    cargar();
})();
