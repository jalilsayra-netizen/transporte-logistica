/* dashboard.js - Resumen de envíos y últimos movimientos */
(async () => {
    const usuario = await iniciarPagina(['ADMIN', 'OPERADOR', 'CONDUCTOR']);
    if (!usuario) return;

    const estados = [
        ['PREPARANDO', 'Preparando', 'borde-preparando'],
        ['EN_TRANSITO', 'En tránsito', 'borde-transito'],
        ['ENTREGADO', 'Entregados', 'borde-entregado'],
        ['CANCELADO', 'Cancelados', 'borde-cancelado']
    ];
    const generales = [
        ['pedidosPendientes', 'Pedidos sin envío'],
        ['vehiculosDisponibles', 'Vehículos disponibles'],
        ['conductoresActivos', 'Conductores activos'],
        ['clientesActivos', 'Clientes activos']
    ];
    const tarjeta = (titulo, valor, borde) => `
        <div class="col-6 col-lg-3">
            <div class="card tarjeta-numero ${borde} shadow-sm"><div class="card-body">
                <div class="text-muted small">${titulo}</div>
                <div class="numero">${valor}</div>
            </div></div>
        </div>`;

    try {
        const resumen = await api('/api/dashboard');
        document.getElementById('tarjetasEnvios').innerHTML =
            estados.map(([k, t, b]) => tarjeta(t, resumen[k] ?? 0, b)).join('');
        if (usuario.rol === 'CONDUCTOR') {
            document.getElementById('bloqueExtra').classList.add('d-none');
        } else {
            document.getElementById('tarjetasExtra').innerHTML =
                generales.map(([k, t]) => tarjeta(t, resumen[k] ?? 0, 'borde-neutro')).join('');
        }

        const envios = (await api('/api/envios')).slice(0, 5);
        document.querySelector('#tablaUltimos tbody').innerHTML = envios.length
            ? envios.map(e => `<tr>
                <td><a href="seguimiento.html?id=${e.idEnvio}">#${e.idEnvio}</a></td>
                <td>${esc(e.pedido.cliente.razonSocial)}</td>
                <td>${esc(e.pedido.destino)}</td>
                <td>${esc(formatearFechaHora(e.fechaSalida))}</td>
                <td>${etiquetaEstado(e.estado.nombre)}</td></tr>`).join('')
            : '<tr><td colspan="5" class="text-center text-muted py-3">Todavía no hay envíos</td></tr>';
    } catch (e) { mostrarError(e); }
})();
